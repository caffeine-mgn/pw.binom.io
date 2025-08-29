package pw.binom.mq.nats.jetstream

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ClosedSendChannelException
import kotlinx.coroutines.channels.ReceiveChannel
import pw.binom.io.Closeable
import pw.binom.mq.nats.client.*
import pw.binom.mq.nats.client.JetStreamApi
import pw.binom.mq.nats.client.dto.ConsumerInfoResponseDto
import pw.binom.mq.nats.client.dto.PullRequestOptionsDto
import pw.binom.uuid.nextUuid
import kotlin.random.Random
import kotlin.time.Duration

class JetStreamConsumer(
  val config: ConsumerConfiguration,
  val streamName: String,
  val consumerName: String,
  private val connection: ReconnactableConnect,
) {
  companion object {
    private class MessageWithAck(
      val client: NatsConnection,
      val replayTo: String,
      val msg: NatsMessage,
    ) : NatsMessage by msg {

      override suspend fun ack() {
        val replyTo = msg.replyTo
        if (replyTo != null) {
          JetStreamApi.sendAck(
            subject = replayTo,
            client = client,
          )
        }
      }
    }

    suspend fun pull(
      streamName: String,
      consumerName: String,
      connection: ReconnactableConnect,
      batch: Int = -1,
      maxBytes: Long = -1,
      expires: Duration = Duration.INFINITE,
      withAck: Boolean,
    ): ReceiveChannel<NatsMessage> {
      val subject = Random.nextUuid().toString()
      val channel = Channel<NatsMessage>()
      println("Making connection...")
      var subscribeListener: Closeable? = null
      val connected = connection.onConnect { con ->
        println("Connected!")
        subscribeListener = con.subscribe(subject = subject, forMessages = batch) { msg ->
          println("Income message $msg")
          if (msg != null) {
            try {
              val replyTo = msg.replyTo
              val clientMsg = if (withAck && replyTo != null) {
                MessageWithAck(
                  client = connection,
                  replayTo = replyTo,
                  msg = msg,
                )
              } else {
                msg
              }
              channel.send(clientMsg)
            } catch (_: ClosedSendChannelException) {
              // Do nothing
            }
          }
        }
        JetStreamApi.pullMessages(
          streamName = streamName,
          consumerName = consumerName,
          into = subject,
          config = PullRequestOptionsDto(
            batch = batch,
            maxBytes = maxBytes,
            noWait = expires == Duration.ZERO,
            expires = if (expires == Duration.ZERO || expires == Duration.INFINITE) null else expires,
          ),
          client = con,
        )
      }
      channel.invokeOnClose {
        subscribeListener?.closeAnyway()
        connected.closeAnyway()
      }
      return channel
    }
  }

  suspend fun pull(
    batch: Int = -1,
    maxBytes: Long = -1,
    expires: Duration = Duration.INFINITE,
  ) = pull(
    streamName = streamName,
    consumerName = consumerName,
    connection = connection,
    batch = batch,
    maxBytes = maxBytes,
    expires = expires,
    withAck = config.ackPolicy != AckPolicy.NONE,
  )
}
