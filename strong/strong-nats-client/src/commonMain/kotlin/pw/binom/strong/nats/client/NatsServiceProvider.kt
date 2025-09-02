package pw.binom.strong.nats.client

import pw.binom.SafeException
import pw.binom.io.Closeable
import pw.binom.io.socket.DomainSocketAddress
import pw.binom.logger.Logger
import pw.binom.logger.debug
import pw.binom.mq.nats.client.*
import pw.binom.network.NetworkManager
import pw.binom.network.tcpConnect
import pw.binom.strong.BeanLifeCycle
import pw.binom.strong.HealthIndicator
import pw.binom.strong.inject
import pw.binom.strong.nats.client.properties.NatsClientProperties
import pw.binom.strong.properties.injectProperty

class NatsServiceProvider : ReconnactableConnect, HealthIndicator {
  private val networkManager: NetworkManager by inject()
  private val properties: NatsClientProperties by injectProperty()
  private var con: ReconnactableConnect? = null
  private val logger = Logger.getLogger("Strong.NatsClient")

  override suspend fun isHealthy(): Boolean {
    val con = con
    if (properties.lazyStart && con == null) {
      return true
    }
    if (con == null) {
      return false
    }
    return con.isConnected
  }

  override val componentName: String
    get() = "Nats Client"

  private suspend fun getConnection(): ReconnactableConnect {
    var con = con
    if (con != null) {
      return con
    }
    logger.debug("Open connection to ${properties.host}:${properties.port}")
    con = ReconnactableConnect.create(
      addresses = setOf(ReconnactableConnect.Address {
        SafeException.async {
          val tcp =
            networkManager.tcpConnect(DomainSocketAddress(host = properties.host, port = properties.port).resolve())
          onException { tcp.closeAnyway() }
          InternalNatsConnection.connect(
            channel = tcp,
            lang = properties.lang,
            clientName = properties.clientName,
            version = properties.clientVersion,
            echo = properties.allowEcho,
          )
        }
      })
    )
    this.con = con
    return con
  }

  init {
    BeanLifeCycle.postConstruct {
      if (!properties.lazyStart) {
        getConnection()
      }
    }

    BeanLifeCycle.preDestroy {
      if (con == null) {
        return@preDestroy
      }
      getConnection().asyncClose()
    }
  }

  override suspend fun asyncClose() {
    throw IllegalStateException("NatsService should be close by strong")
  }

  override suspend fun send(
    subject: String,
    headers: HeadersBody,
    replyTo: String?,
    data: ByteArray?,
  ) = getConnection().send(
    subject = subject,
    headers = headers,
    replyTo = replyTo,
    data = data,
  )

  override suspend fun subscribe(
    subject: String,
    group: String?,
    forMessages: Int,
  ) = getConnection().subscribe(
    subject = subject,
    group = group,
    forMessages = forMessages,
  )

  override suspend fun subscribe(
    subject: String,
    group: String?,
    forMessages: Int,
    listener: suspend (NatsMessage?) -> Unit,
  ) = getConnection().subscribe(
    subject = subject,
    group = group,
    forMessages = forMessages,
    listener = listener,
  )

  override suspend fun sendAndReceive(
    subject: String,
    headers: HeadersBody,
    data: ByteArray?,
  ) = getConnection().sendAndReceive(
    subject = subject,
    headers = headers,
    data = data,
  )

  override val isConnected: Boolean
    get() = con?.isConnected != false

  override suspend fun onConnect(func: suspend (NatsConnection) -> Unit): Closeable =
    getConnection().onConnect(func)

  override suspend fun onDisconnected(func: suspend (NatsConnection) -> Unit): Closeable =
    getConnection().onDisconnected(func)
}
