package pw.binom.mq.nats.client

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import pw.binom.io.AsyncCloseable
import pw.binom.io.Closeable
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

interface ReconnactableConnect : AsyncCloseable, NatsConnection {
  companion object {
    fun create(
      /**
       * Список адресов
       */
      addresses: Set<Address>,

      /**
       * Вызывается в случае неожиданного подключения
       */
      unexpectedMessageListener: ((NatsMessage) -> Unit)? = null,
      scope: CoroutineScope = GlobalScope,
      context: CoroutineContext = EmptyCoroutineContext,
    ): ReconnactableConnect = ReconnactableConnectImpl(
      addresses = addresses,
      unexpectedMessageListener = unexpectedMessageListener,
      scope = scope,
      context = context,
    )
  }
  val isConnected: Boolean
  fun interface Address {
    suspend fun connect(): NatsProtoConnection
  }

  /**
   * Вызывается когда соединение установлено
   */
  suspend fun onConnect(func: suspend (NatsConnection) -> Unit): Closeable

  /**
   * Вызывается когда соединение потеряно
   */
  fun onDisconnected(func: suspend (NatsConnection) -> Unit): Closeable
}
