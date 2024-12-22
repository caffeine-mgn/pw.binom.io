package pw.binom.mq.nats

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import pw.binom.DEFAULT_BUFFER_SIZE
import pw.binom.io.AsyncChannel
import pw.binom.io.socket.SocketAddress
import pw.binom.mq.MqConnection
import pw.binom.mq.nats.client.Auth
import pw.binom.mq.nats.client.InternalNatsConnection
import pw.binom.mq.nats.client.NatsReader
import pw.binom.network.NetworkManager
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

suspend fun MqConnection.Companion.nats(
  channel: AsyncChannel,
  clientName: String? = null,
  lang: String = "kotlin",
  echo: Boolean = true,
  tlsRequired: Boolean = false,
  version: String = "0.1.x",
  headers: Boolean = true,
  auth: Auth? = null,
  readBufferSize: Int = DEFAULT_BUFFER_SIZE,
  writeBufferSize: Int = DEFAULT_BUFFER_SIZE,
  scope: CoroutineScope = GlobalScope,
  context: CoroutineContext = DefaultEmptyCoroutineContext,
): NatsMqConnection {
  val connection =
    InternalNatsConnection.connect(
      channel = channel,
      clientName = clientName,
      lang = lang,
      echo = echo,
      tlsRequired = tlsRequired,
      version = version,
      headers = headers,
      auth = auth,
      readBufferSize = readBufferSize,
      writeBufferSize = writeBufferSize,
    )
  val reader =
    NatsReader.start(
      con = connection,
      scope = scope,
      context = if (context === DefaultEmptyCoroutineContext) coroutineContext else context,
    )
  return NatsMqConnectionImpl(reader)
}

suspend fun MqConnection.Companion.nats(
  address: SocketAddress,
  clientName: String? = null,
  lang: String = "kotlin",
  echo: Boolean = true,
  tlsRequired: Boolean = false,
  version: String = "0.1.x",
  headers: Boolean = true,
  auth: Auth? = null,
  reconnectDelay: Duration = 5.seconds,
  readBufferSize: Int = DEFAULT_BUFFER_SIZE,
  writeBufferSize: Int = DEFAULT_BUFFER_SIZE,
  networkManager: NetworkManager,
  scope: CoroutineScope = GlobalScope,
  context: CoroutineContext = DefaultEmptyCoroutineContext,
): NatsMqConnection {
  val connection =
    ReconnectableNatsProtoConnection(
      address = address,
      clientName = clientName,
      lang = lang,
      echo = echo,
      tlsRequired = tlsRequired,
      version = version,
      headers = headers,
      auth = auth,
      readBufferSize = readBufferSize,
      writeBufferSize = writeBufferSize,
      reconnectDelayConfig = { reconnectDelay },
      networkManager = networkManager,
    )
  val reader =
    NatsReader.start(
      con = connection,
      scope = scope,
      context = if (context === DefaultEmptyCoroutineContext) coroutineContext else context,
    )
  return NatsMqConnectionImpl(reader)
}
