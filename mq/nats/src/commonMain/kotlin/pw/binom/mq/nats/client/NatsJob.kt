package pw.binom.mq.nats.client

import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.cancelAndJoin
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
import pw.binom.io.Closeable
import pw.binom.io.IOException
import pw.binom.mq.nats.exceptions.NatsConnectionClosedException
import pw.binom.mq.nats.exceptions.SubscribeFinishedException
import pw.binom.network.SocketClosedException
import pw.binom.uuid.nextUuid
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.cancellation.CancellationException
import kotlin.coroutines.coroutineContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.random.Random

class NatsJob(
  private val connection: NatsProtoConnection,
  private val unexpectedMessageListener: ((NatsMessage) -> Unit)?,
  scope: CoroutineScope = GlobalScope,
  context: CoroutineContext = EmptyCoroutineContext,
  private val onDisconnected: (suspend (NatsJob) -> Unit)? = null,
) : NatsConnection {
  private val longListeners = HashMap<String, Listener>()
  private val oneShortListeners = HashMap<String, CancellableContinuation<NatsMessage>>()
  private val oneShortListenersLock = SpinLock()
  private val longListenersLock = SpinLock()
  private val oneShortCounter = AtomicLong(0)
  private val connected = AtomicBoolean(true)
  private val reading = AtomicBoolean(false)

  private abstract inner class Listener {
    abstract val subscribeId: String
    abstract var forMessages: Int
    abstract suspend fun push(msg: NatsMessage)
    abstract fun finished()
    abstract suspend fun disconnected()
    fun decMessage() {
      if (forMessages > 0) {
        forMessages--
        if (forMessages <= 0) {
          longListenersLock.synchronize {
            longListeners.remove(subscribeId)
            finished()
          }
        }
      }
    }
  }

  private inner class CallbackListener(
    override val subscribeId: String,
    override var forMessages: Int,
    val callback: suspend (NatsMessage?) -> Unit,
  ) : Listener() {
    override fun finished() {
      // Do nothing
    }

    override suspend fun push(msg: NatsMessage) {
      callback(msg)
      decMessage()
    }

    override suspend fun disconnected() {
      callback(null)
    }
  }

  private inner class ChannelListener(
    override val subscribeId: String,
    val channel: Channel<NatsMessage>,
    override var forMessages: Int,
  ) : Listener() {
    override suspend fun push(msg: NatsMessage) {
      try {
        channel.send(msg)
      } catch (_: ClosedSendChannelException) {
        longListenersLock.synchronize {
          longListeners.remove(subscribeId)
        }
      }
      decMessage()
    }

    override fun finished() {
      channel.close(SubscribeFinishedException())
    }

    override suspend fun disconnected() {
      channel.close(NatsConnectionClosedException())
    }
  }

  private val subscribePrefix = "one-shot-"
  private val subjectPrefix = "one-shot-${Random.nextUuid()}"

  private val job = scope.launch(context) {
    try {
      runBlocking()
    } catch (e: CancellationException) {
      disconnected()
      return@launch
    } catch (_: IOException) {
      disconnected()
      return@launch
    }
    disconnected()
  }

  private suspend fun runBlocking() {
    check(reading.compareAndSet(false, true)) { "Reading process already started" }
    while (coroutineContext.isActive) {
      val msg = try {
        connection.readMessage()
      } catch (_: CancellationException) {
        break
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

  private suspend fun disconnected() {
    if (!connected.compareAndSet(true, false)) {
      return
    }
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
      it.value.disconnected()
    }
    oneShortListeners.forEach {
      it.value.resumeWithException(NatsConnectionClosedException())
    }
    onDisconnected?.invoke(this)
    job.cancel()
  }

  override suspend fun subscribe(
    subject: String,
    group: String?,
    forMessages: Int,
    listener: suspend (NatsMessage?) -> Unit,
  ): Closeable {
    require(forMessages == -1 || forMessages > 0) { "Invalid argument \"forMessages\". Should be equals -1 or more than zero" }
    val id = oneShortCounter.addAndGet(1)
    val subscribeId = "$subjectPrefix-$id"
    val listener = CallbackListener(subscribeId = subscribeId, forMessages = forMessages, callback = listener)

    longListenersLock.synchronize {
      longListeners[subscribeId] = listener
    }
    try {
      connection.subscribe(subject = subject, group = group, subscribeId = subscribeId)
      if (forMessages > 0) {
        connection.unsubscribe(subscribeId = subscribeId, afterMessages = forMessages)
      }
    } catch (e: IOException) {
      longListenersLock.synchronize {
        longListeners.remove(subscribeId)
      }
      throw e
    }
    val context = coroutineContext
    return Closeable {
      CoroutineScope(context).launch {
        longListenersLock.synchronize {
          longListeners.remove(subscribeId)
        }
        connection.unsubscribe(subscribeId = subscribeId)
      }
    }
  }

  override suspend fun subscribe(
    subject: String,
    group: String?,
    forMessages: Int,
  ): ReceiveChannel<NatsMessage> {
    require(forMessages == -1 || forMessages > 0) { "Invalid argument \"forMessages\". Should be equals -1 or more than zero" }
    val id = oneShortCounter.addAndGet(1)
    val subscribeId = "$subjectPrefix-$id"
    val listener = ChannelListener(subscribeId = subscribeId, channel = Channel(), forMessages = forMessages)

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

  override suspend fun send(
    subject: String,
    headers: HeadersBody,
    replyTo: String?,
    data: ByteArray?,
  ) = connection.publish(
    subject = subject,
    replyTo = replyTo,
    headers = headers,
    data = data,
  )

  override suspend fun sendAndReceive(
    subject: String,
    headers: HeadersBody,
    data: ByteArray?,
  ): NatsMessage {
    val id = oneShortCounter.addAndGet(1)
    val subscribeId = "$subscribePrefix-$id"
//    val responseSubject = "$subjectPrefix-$id"
    try {
      connection.subscribe(
        subscribeId = subscribeId,
        subject = subscribeId,
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
    val context = coroutineContext
    return suspendCancellableCoroutine { coroutine ->
      coroutine.invokeOnCancellation {
        oneShortListeners.remove(subscribeId)
        CoroutineScope(context).launch {
          try {
            connection.unsubscribe(subscribeId = subscribeId)
          } catch (_: IOException) {
            // Do nothing
          }
        }
      }
      oneShortListeners[subscribeId] = coroutine
    }
  }

  override suspend fun asyncClose() {
    job.cancelAndJoin()
    connection.asyncClose()
  }
}
