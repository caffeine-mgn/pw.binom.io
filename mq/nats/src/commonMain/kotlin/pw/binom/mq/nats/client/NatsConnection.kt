package pw.binom.mq.nats.client

import kotlinx.coroutines.Dispatchers
import pw.binom.io.AsyncCloseable
import pw.binom.io.ByteBuffer
import pw.binom.io.socket.NetworkAddress
import pw.binom.network.Network
import pw.binom.network.NetworkManager
import pw.binom.uuid.nextUuid
import kotlin.random.Random

interface NatsConnection : AsyncCloseable {
  companion object {
    /**
     * Creates new nats connection
     */
    fun create(
      clientName: String? = null,
      lang: String = "kotlin",
      echo: Boolean = true,
      tlsRequired: Boolean = false,
      user: String? = null,
      pass: String? = null,
      defaultGroup: String? = null,
      attemptCount: Int = 3,
      networkDispatcher: NetworkManager = Dispatchers.Network,
      serverList: List<NetworkAddress>,
    ): NatsConnection =
      NatsConnectorImpl(
        clientName = clientName,
        lang = lang,
        echo = echo,
        tlsRequired = tlsRequired,
        user = user,
        pass = pass,
        defaultGroup = defaultGroup,
        attemptCount = attemptCount,
        networkDispatcher = networkDispatcher,
        serverList = serverList,
      )
  }

  val config: ConnectInfo

  suspend fun subscribe(
    subject: String,
    group: String? = null,
    subscribeId: String,
  )

  suspend fun unsubscribe(
    id: String,
    afterMessages: Int = 0,
  )

  suspend fun subscribeEx(
    subject: String,
    group: String? = null,
  ): AsyncCloseable {
    val subscribeId = "subscribe-" + Random.nextUuid().toString()
    subscribe(
      subject = subject,
      subscribeId = subscribeId,
      group = group,
    )
    return AsyncCloseable {
      unsubscribe(
        id = subscribeId,
      )
    }
  }

  suspend fun readMessage(): NatsMessage

  suspend fun publish(
    subject: String,
    replyTo: String? = null,
    headers: HeadersBody = HeadersBody.empty,
    data: ByteArray?,
  )

  suspend fun publish(
    subject: String,
    replyTo: String? = null,
    headers: HeadersBody = HeadersBody.empty,
    data: ByteBuffer?,
  )
}
