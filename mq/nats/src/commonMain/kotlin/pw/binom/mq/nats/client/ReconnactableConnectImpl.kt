package pw.binom.mq.nats.client

import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import pw.binom.concurrency.SpinLock
import pw.binom.concurrency.synchronize
import pw.binom.io.AsyncCloseable
import pw.binom.io.Closeable
import pw.binom.mq.nats.exceptions.SubscribeFinishedException
import pw.binom.uuid.nextUuid
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.cancellation.CancellationException
import kotlin.coroutines.resume
import kotlin.random.Random
import kotlin.time.Duration.Companion.seconds

internal class ReconnactableConnectImpl(
  /**
   * Список адресов
   */
  addresses: Set<ReconnactableConnect.Address>,

  /**
   * Вызывается в случае неожиданного подключения
   */
  private val unexpectedMessageListener: ((NatsMessage) -> Unit)? = null,
  val scope: CoroutineScope = GlobalScope,
  val context: CoroutineContext = EmptyCoroutineContext,
) : ReconnactableConnect {
  init {
    require(addresses.isNotEmpty()) { "Addresses should not be empty" }
  }

  private val connections = HashMap<ReconnactableConnect.Address, NatsProtoConnection>()
  private val onConnectHandlers = ArrayList<suspend (NatsConnection) -> Unit>()
  private val onConnectOnceHandlers = HashSet<CancellableContinuation<NatsConnection>>()
  private val onDisconnectHandlers = ArrayList<suspend (NatsConnection) -> Unit>()
  private val listenersLock = SpinLock()
  private val addresses = addresses//.shuffled()

  private var currentConnection: NatsJob? = null
  override val isConnected
    get() = currentConnection != null

  private suspend fun connectToServer() {
    val addresses = addresses.shuffled()
    addresses.forEach { address ->
      val proto = address.connect()
      currentConnection = NatsJob(
        connection = proto,
        unexpectedMessageListener = unexpectedMessageListener,
        scope = scope,
        context = context,
        onDisconnected = { connection ->
          currentConnection = null
          listenersLock.synchronize { ArrayList(onDisconnectHandlers) }.forEach {
            it(connection)
          }
        },
      )
      listenersLock.synchronize { ArrayList(onConnectHandlers) }.forEach {
        it(currentConnection!!)
      }
      listenersLock.synchronize {
        val e = HashSet(onConnectOnceHandlers)
        onConnectOnceHandlers.clear()
        e
      }.forEach {
        it.resume(currentConnection!!)
      }
    }

  }

  val job = scope.launch(context) {
    while (isActive) {
      try {
        if (currentConnection == null) {
          connectToServer()
        }
        delay(5.seconds)
      } catch (_: CancellationException) {
        break
      } catch (_: Throwable) {
        delay(2.seconds)
      }
    }
  }

  private suspend fun getConnection(): NatsConnection {
    val currentConnection = currentConnection
    if (currentConnection != null) {
      return currentConnection
    }
    val e = suspendCancellableCoroutine { con ->
      con.invokeOnCancellation {
        listenersLock.synchronize {
          onConnectOnceHandlers -= con
        }
      }
      listenersLock.synchronize {
        onConnectOnceHandlers += con
      }
    }
    return e
  }

  override suspend fun onConnect(func: suspend (NatsConnection) -> Unit): Closeable {
    val currentConnection = currentConnection
    if (currentConnection != null) {
      func(currentConnection)
    }
    listenersLock.synchronize {
      onConnectHandlers += func
    }
    return Closeable {
      listenersLock.synchronize {
        onConnectHandlers -= func
      }
    }
  }

  override suspend fun onDisconnected(func: suspend (NatsConnection) -> Unit): Closeable {
    listenersLock.synchronize {
      onDisconnectHandlers += func
    }
    return Closeable {
      listenersLock.synchronize {
        onDisconnectHandlers -= func
      }
    }
  }

  override suspend fun asyncClose() {
    job.cancelAndJoin()
    currentConnection?.asyncClose()
    currentConnection = null
  }

  override suspend fun send(subject: String, headers: HeadersBody, replyTo: String?, data: ByteArray?) {
    getConnection().send(
      subject = subject,
      headers = headers,
      replyTo = replyTo,
      data = data,
    )
  }

  override suspend fun subscribe(subject: String, group: String?, forMessages: Int): ReceiveChannel<NatsMessage> {
    val channel = Channel<NatsMessage>()
    var forMessages = forMessages
    var reconnectListener: Closeable? = null
    var listener: Closeable? = null
    reconnectListener = onConnect {
      listener = it.subscribe(
        subject = subject,
        group = group,
        forMessages = forMessages,
      ) {
        if (it != null) {
          channel.send(it)
          if (forMessages > 0) {
            forMessages--
            if (forMessages == 0) {
              reconnectListener?.close()
              channel.close(SubscribeFinishedException())
            }
          }
        }
      }
    }
    channel.invokeOnClose {
      reconnectListener.close()
      listener?.close()
    }
    return channel
  }

  override suspend fun subscribe(
    subject: String,
    group: String?,
    forMessages: Int,
    listener: suspend (NatsMessage?) -> Unit,
  ): Closeable {
    var forMessages = forMessages
    var reconnectListener: Closeable? = null
    var listener: Closeable? = null

    reconnectListener = onConnect {
      listener = it.subscribe(
        subject = subject,
        group = group,
        forMessages = forMessages,
      ) {
        if (it != null) {
          listener(it)
          if (forMessages > 0) {
            forMessages--
            if (forMessages == 0) {
              reconnectListener?.close()
            }
          }
        }
      }
    }
    return Closeable {
      reconnectListener.close()
      listener?.close()
    }
  }

  override suspend fun sendAndReceive(
    subject: String,
    headers: HeadersBody,
    data: ByteArray?,
  ): NatsMessage {
    val replaySubject = Random.nextUuid().toString()
    var closable: Closeable? = null
    var subscribeClosable: Closeable? = null
    var water: CancellableContinuation<NatsMessage>? = null
    closable = onConnect {
      subscribeClosable = subscribe(
        subject = replaySubject,
        group = null,
        forMessages = 1,
      ) { msg ->
        if (msg != null) {
          water?.resume(msg)
          closable?.close()
          subscribeClosable?.close()
        }
      }
    }
    send(subject = subject, headers = headers, replyTo = replaySubject, data = data)
    return suspendCancellableCoroutine {
      it.invokeOnCancellation {
        subscribeClosable?.close()
        closable.close()
      }
      water = it
    }
  }
}
