package pw.binom.mq.nats

import pw.binom.atomic.AtomicBoolean
import pw.binom.concurrency.SpinLock
import pw.binom.concurrency.synchronize
import pw.binom.io.AsyncCloseable
import pw.binom.mq.Consumer
import pw.binom.mq.Message
import pw.binom.mq.nats.client.AckPolicy
import pw.binom.mq.nats.client.ConsumerConfiguration
import pw.binom.mq.nats.client.dto.PullRequestOptionsDto

class JetStreamConsumer(
  val config: ConsumerConfiguration,
  val topic: JetStreamTopic,
  val incomeListener: suspend (Message) -> Unit,
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
        listener =
          this.topic.connection.js.receiveMessage(
            streamName = this.topic.config.name,
            consumerName = this.config.name!!,
            config = PullRequestOptionsDto(batch = batchSize),
            incomeListener = incomeListener,
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

  override suspend fun deleteAndClose() {
    asyncCloseAnyway()
    topic.connection.js.deleteConsumer(
      streamName = topic.config.name,
      consumerName = config.name!!,
    )
  }

  override val isReceiving: Boolean
    get() = receiving.getValue()

  override suspend fun asyncClose() {
    stop()
  }
}
