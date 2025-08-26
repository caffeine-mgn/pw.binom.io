package pw.binom.mq.nats.client

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
import pw.binom.io.socket.DomainSocketAddress
import pw.binom.mq.nats.exceptions.SubscribeFinishedException
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Duration.Companion.seconds

class ReconnactableConnect(
  /**
   * Функция подключения.
   * Принимает адрес подключения, возвращает котовое подключение
   */
  val connect: suspend (Address) -> NatsProtoConnection,

  /**
   * Список адресов
   */
  addresses: Set<Address>,

  /**
   * Вызывается в случае неожиданного подключения
   */
  private val unexpectedMessageListener: ((NatsMessage) -> Unit)? = null,
  val scope: CoroutineScope = GlobalScope,
  val context: CoroutineContext = EmptyCoroutineContext,
) : AsyncCloseable, NatsConnection {
  init {
    require(addresses.isNotEmpty()) { "Addresses should not be empty" }
  }

  data class Address(
    val address: DomainSocketAddress,
    val auth: Auth?,
  )

  private val connections = HashMap<Address, NatsProtoConnection>()
  private val onConnectHandlers = ArrayList<suspend (NatsConnection) -> Unit>()
  private val onDisconnectHandlers = ArrayList<suspend (NatsConnection) -> Unit>()
  private val listenersLock = SpinLock()
  private val addresses = addresses//.shuffled()

  private var currentConnection: NatsJob? = null
  val isConnected
    get() = currentConnection != null

  private suspend fun connectToServer() {
    val addresses = addresses.shuffled()
    addresses.forEach { address ->
      val proto = connect(address)
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

  /**
   * Вызывается когда соединение установлено
   */
  suspend fun onConnect(func: suspend (NatsConnection) -> Unit): Closeable {
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

  /**
   * Вызывается когда соединение потеряно
   */
  fun onDisconnected(func: suspend (NatsConnection) -> Unit): Closeable {
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
    var reconnectListener: Closeable? = null

    reconnectListener = onConnect {
      it.send(
        subject = subject,
        headers = headers,
        replyTo = replyTo,
        data = data,
      )
      reconnectListener?.close()
    }
    TODO()
    suspendCancellableCoroutine<Unit> {

    }
    TODO("Not yet implemented")
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
}
