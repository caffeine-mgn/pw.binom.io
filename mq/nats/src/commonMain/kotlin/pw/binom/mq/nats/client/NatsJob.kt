package pw.binom.mq.nats.client

import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ClosedSendChannelException
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import pw.binom.atomic.AtomicBoolean
import pw.binom.atomic.AtomicLong
import pw.binom.concurrency.SpinLock
import pw.binom.concurrency.synchronize
import pw.binom.io.IOException
import pw.binom.mq.nats.exceptions.NatsConnectionClosedException
import pw.binom.mq.nats.exceptions.SubscribeFinishedException
import pw.binom.network.SocketClosedException
import pw.binom.uuid.nextUuid
import kotlin.coroutines.coroutineContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.random.Random

class NatsJob(
  private val connection: NatsProtoConnection,
  private val unexpectedMessageListener: ((NatsMessage) -> Unit)?,
) {
  private val longListeners = HashMap<String, Listener>()
  private val oneShortListeners = HashMap<String, CancellableContinuation<NatsMessage>>()
  private val oneShortListenersLock = SpinLock()
  private val longListenersLock = SpinLock()
  private val oneShortCounter = AtomicLong(0)
  private val connected = AtomicBoolean(true)
  private val reading = AtomicBoolean(true)

  private inner class Listener(
    val subscribeId: String,
    val channel: Channel<NatsMessage>,
    private var forMessages: Int,
  ) {
    suspend fun push(msg: NatsMessage) {
      try {
        channel.send(msg)
      } catch (_: ClosedSendChannelException){
        longListenersLock.synchronize {
          longListeners.remove(subscribeId)
        }
      }
      if (forMessages > 0) {
        forMessages--
        if (forMessages <= 0) {
          longListenersLock.synchronize {
            longListeners.remove(subscribeId)
            channel.close(SubscribeFinishedException())
          }
        }
      }
    }
  }

  private val subscribePrefix = "one-shot-"
  private val subjectPrefix = "one-shot-${Random.nextUuid()}"

  suspend fun runBlocking() {
    check(reading.compareAndSet(false, true)) { "Reading process already started" }
    while (coroutineContext.isActive) {
      val msg = try {
        connection.readMessage()
      } catch (e: SocketClosedException) {
        disconnected()
        throw e
      }

      val subscribeId = msg.subscribeId
      if (subscribeId.startsWith(subscribePrefix)) {
        val listener = oneShortListeners.remove(subscribeId)
        if (listener != null) {
          listener.resume(msg)
          continue
        }
      }
      val listener = longListeners[subscribeId]
      if (listener != null) {
        listener.push(msg)
        continue
      }

      unexpectedMessageListener?.invoke(msg)
    }
  }

  private fun disconnected() {
    connected.setValue(false)
    val longListeners = longListenersLock.synchronize {
      val listeners = HashMap(longListeners)
      longListeners.clear()
      listeners
    }
    val oneShortListeners = oneShortListenersLock.synchronize {
      val listeners = HashMap(oneShortListeners)
      oneShortListeners.clear()
      listeners
    }
    longListeners.forEach {
      it.value.channel.close(NatsConnectionClosedException())
    }
    oneShortListeners.forEach {
      it.value.resumeWithException(NatsConnectionClosedException())
    }
  }

  suspend fun subscribe(
    subject: String,
    group: String,
    forMessages: Int = -1,
  ): ReceiveChannel<NatsMessage> {
    require(forMessages == -1 || forMessages > 0) { "Invalid argument \"forMessages\". Should be equals -1 or more than zero" }
    val id = oneShortCounter.addAndGet(1)
    val subscribeId = "$subjectPrefix-$id"
    val listener = Listener(subscribeId = subscribeId, channel = Channel(), forMessages = forMessages)

    longListenersLock.synchronize {
      longListeners[subscribeId] = listener
    }
    try {
      connection.subscribe(subject = subject, group = group, subscribeId = subscribeId)
      if (forMessages > 0) {
        connection.unsubscribe(subscribeId = subscribeId, afterMessages = forMessages)
      }
    } catch (e: IOException) {
      listener.channel.close()
      listener.channel.cancel()
      longListenersLock.synchronize {
        longListeners.remove(subscribeId)
      }
      throw e
    }
    val context = coroutineContext
    listener.channel.invokeOnClose {
      if (connected.getValue()) {
        longListenersLock.synchronize {
          longListeners.remove(subscribeId)
        }
        if (connected.getValue()) {
          CoroutineScope(context).launch {
            try {
              connection.unsubscribe(subscribeId = subscribeId)
            } catch (_: IOException) {
              // Do nothing
            }
          }
        }
      }
    }
    return listener.channel
  }

  suspend fun sendAndReceive(
    subject: String,
    headers: HeadersBody = HeadersBody.empty,
    data: ByteArray?,
  ): NatsMessage {
    val id = oneShortCounter.addAndGet(1)
    val subscribeId = "$subscribePrefix-$id"
    val responseSubject = "$subjectPrefix-$id"
    try {
      connection.subscribe(
        subscribeId = subscribeId,
        subject = responseSubject,
        forMessages = 1,
      )
      connection.publish(
        subject = subject,
        replyTo = subscribeId,
        headers = headers,
        data = data,
      )
    } catch (e: IOException) {
      disconnected()
      throw e
    }
    return suspendCancellableCoroutine { coroutine ->
      coroutine.invokeOnCancellation {
        oneShortListeners.remove(subscribeId)
      }
      oneShortListeners[subscribeId] = coroutine
    }
  }
}
