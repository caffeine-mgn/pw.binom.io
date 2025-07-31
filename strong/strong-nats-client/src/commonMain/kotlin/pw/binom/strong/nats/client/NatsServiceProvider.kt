package pw.binom.strong.nats.client

import kotlinx.coroutines.channels.ReceiveChannel
import pw.binom.io.socket.DomainSocketAddress
import pw.binom.logger.Logger
import pw.binom.logger.debug
import pw.binom.mq.MqConnection
import pw.binom.mq.nats.JetStreamMqConnection
import pw.binom.mq.nats.NatsMqConnection
import pw.binom.mq.nats.ReconnectableNatsProtoConnection
import pw.binom.mq.nats.client.Auth
import pw.binom.mq.nats.client.NatsMessage
import pw.binom.mq.nats.client.NatsReader
import pw.binom.mq.nats.nats
import pw.binom.network.NetworkManager
import pw.binom.strong.BeanLifeCycle
import pw.binom.strong.HealthIndicator
import pw.binom.strong.inject
import pw.binom.strong.nats.client.properties.NatsClientProperties
import pw.binom.strong.properties.injectProperty

class NatsServiceProvider : NatsMqConnection, HealthIndicator {
  private val networkManager: NetworkManager by inject()
  private val properties: NatsClientProperties by injectProperty()
  private var con: NatsMqConnection? = null
  private val logger = Logger.getLogger("Strong.NatsClient")

  override suspend fun isHealthy(): Boolean {
    val con = con
    if (properties.lazyStart && con == null) {
      return true
    }
    if (con == null) {
      return false
    }
    val connection = con.reader.connection
    if (connection is ReconnectableNatsProtoConnection) {
      return connection.isConnected()
    }
    return true
  }

  override val componentName: String
    get() = "Nats Client"

  private suspend fun getConnection(): NatsMqConnection {
    var con = con
    if (con != null) {
      return con
    }
    logger.debug("Open connection to ${properties.host}:${properties.port}")
    con = MqConnection.nats(
      address = DomainSocketAddress(properties.host, properties.port),
      networkManager = networkManager,
      lang = properties.lang,
      context = networkManager,
      clientName = properties.clientName,
      version = properties.clientVersion,
      echo = properties.allowEcho,
      reconnectDelay = properties.reconnectDelay,
      auth = properties.auth?.let {
        Auth(user = it.user, password = it.password)
      },
    )
    internalJetStream = con.jetStream
    internalReader = con.reader
    this.con = con
    return con
  }

  private var internalJetStream: JetStreamMqConnection? = null
  private var internalReader: NatsReader? = null

  override val jetStream: JetStreamMqConnection?
    get() = internalJetStream
  override val reader: NatsReader
    get() = internalReader ?: TODO("Nets connection not created")

  override suspend fun createTopic(name: String) =
    getConnection().createTopic(name)

  override suspend fun getTopic(name: String) =
    getConnection().getTopic(name)

  override suspend fun getOrCreateTopic(name: String) =
    getConnection().getOrCreateTopic(name)

  override suspend fun listen(
    subject: String,
    group: String?,
    messageCount: Int,
  ): ReceiveChannel<NatsMessage> = getConnection().listen(
    subject = subject,
    group = group,
    messageCount = messageCount
  )

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
}
