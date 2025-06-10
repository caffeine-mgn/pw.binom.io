package pw.binom.mq.nats

import kotlinx.coroutines.channels.Channel
import pw.binom.atomic.AtomicBoolean
import pw.binom.concurrency.SpinLock
import pw.binom.concurrency.synchronize
import pw.binom.io.AsyncCloseable
import pw.binom.mq.Consumer
import pw.binom.mq.nats.client.AckPolicy
import pw.binom.mq.nats.client.ConsumerConfiguration
import pw.binom.mq.nats.client.NatsHeaders
import pw.binom.mq.nats.client.NatsMessage
import pw.binom.mq.nats.client.dto.PullRequestOptionsDto
import kotlin.time.Duration

class JetStreamAutoConsumer(
  val config: ConsumerConfiguration,
  val topic: JetStreamTopic,
  val incomeListener: suspend (NatsMessage) -> Unit,
  var batchSize: Int,
) : Consumer {
  private var listener: AsyncCloseable? = null

  //  private var subject = ""
  private val listenerLock = SpinLock()

//  private inner class MessageWithAck(val msg: NatsMessage) : Message {
//    override val headers: Headers
//      get() = msg.headers
//    override val topic: String
//      get() = msg.topic
//    override val body: ByteArray
//      get() = msg.body
//
//    override suspend fun ack() {
//      val replyTo = msg.replyTo
//      if (replyTo != null) {
//        this@JetStreamConsumer.topic.connection.js.sendAck(subject = replyTo)
//      }
//    }
//
//    override fun toString(): String = msg.toString()
//  }

  //  private var remaining = 0
  private val receiving = AtomicBoolean(false)

//  private suspend fun pullNext() {
//    remaining = batchSize
//    topic.connection.js.pullMessages(
//      streamName = topic.config.name,
//      consumerName = config.name!!,
//      into = subject,
//      config = PullRequestOptionsDto(batch = batchSize),
//    )
//  }

  override suspend fun start() {
    listenerLock.synchronize {
      receiving.setValue(true)
      if (listener == null) {
        val name = this.config.name ?: this.config.durableName!!
        listener =
          this.topic.connection.js.receiveMessage(
            streamName = this.topic.config.name,
            consumerName = name,
            config = PullRequestOptionsDto(batch = batchSize),
            incomeListener = { msg ->

              if (msg.headers.code == NatsHeaders.CODE_CONSUME_TIMEOUT) {
                stop()
                return@receiveMessage
              }
              if (msg.headers.code == NatsHeaders.CODE_CONSUMER_DELETED) {
                stop()
                deleteWaiter.send(Unit)
              }
              incomeListener(msg)
            },
            withAckSupport = config.ackPolicy != AckPolicy.NONE,
          )
      }
    }
  }

  override suspend fun stop() {
    listenerLock.synchronize {
      val old = listener ?: return
      receiving.setValue(false)
      listener = null
//      subject = ""
      old
    }.asyncCloseAnyway()
  }

  private val deleteWaiter = Channel<Unit>()

  override suspend fun deleteAndClose() {
    topic.connection.js.deleteConsumer(
      streamName = topic.config.name,
      consumerName = config.name!!,
    )
    deleteWaiter.receive()
  }

  override val isReceiving: Boolean
    get() = receiving.getValue()

  override suspend fun asyncClose() {
    stop()
  }
}
