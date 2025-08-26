package pw.binom.mq.nats.client

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import pw.binom.concurrency.SpinLock
import pw.binom.concurrency.synchronize
import pw.binom.io.AsyncCloseable
import pw.binom.io.Closeable
import pw.binom.io.socket.DomainSocketAddress
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
) : AsyncCloseable {
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
}
