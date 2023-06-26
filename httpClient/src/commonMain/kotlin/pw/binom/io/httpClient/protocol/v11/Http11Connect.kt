package pw.binom.io.httpClient.protocol.v11

import pw.binom.DEFAULT_BUFFER_SIZE
import pw.binom.io.*
import pw.binom.io.http.Headers
import pw.binom.io.httpClient.*
import pw.binom.io.httpClient.getPort
import pw.binom.io.httpClient.protocol.ConnectionPoll
import pw.binom.io.httpClient.protocol.HttpConnect
import pw.binom.io.socket.InetNetworkAddress
import pw.binom.network.NetworkManager
import pw.binom.network.tcpConnect
import pw.binom.url.URL
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.TimeSource

@OptIn(ExperimentalTime::class)
class Http11Connect(
    private val networkManager: NetworkManager,
    private var tcp: AsyncChannel?,
    defaultKeepAliveTimeout: Duration,
) : HttpConnect {
    private var created = TimeSource.Monotonic.markNow()
    private var closed = false
    override val isAlive: Boolean
        get() = !closed && created.elapsedNow() < timeout
    override val age: Duration
        get() = created.elapsedNow()
    private var timeout = defaultKeepAliveTimeout

    override suspend fun makePostRequest(
        pool: ConnectionPoll,
        method: String,
        url: URL,
        headers: Headers,
    ): HttpRequestBody {
        created = TimeSource.Monotonic.markNow()
        val newKey = "${url.schema}://${url.host}${url.port?.let { ":$it" } ?: ""}"
        var tcp = tcp
        if (tcp == null) {
            tcp = networkManager.tcpConnect(
                InetNetworkAddress.create(
                    host = url.host,
                    port = url.port ?: url.getPort(),
                ),
            )
            this.tcp = tcp
        }
        val output = tcp.bufferedAsciiWriter(closeParent = false)
        val input = tcp.bufferedAsciiReader(closeParent = false)
        Http11ConnectFactory2.sendRequest(
            output = output,
            method = method,
            request = url.request,
            headers = headers,
        )
        output.flush()

        return Http11RequestBody(
            headers = headers,
            autoFlushBuffer = DEFAULT_BUFFER_SIZE,
            input = input,
            output = output,
            requestFinishedListener = { responseKeepAlive, success ->
                closed = !(headers.keepAlive ?: true) || !responseKeepAlive || !success
                pool.recycle(key = newKey, connect = this)
            },
        )
    }

    override suspend fun asyncClose() {
        tcp?.asyncClose()
        closed = true
    }
}
