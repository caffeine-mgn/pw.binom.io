package pw.binom.io.httpClient

import pw.binom.DEFAULT_BUFFER_SIZE
import pw.binom.io.AsyncChannel
import pw.binom.io.socket.ssl.asyncChannel
import pw.binom.network.NetworkManager
import pw.binom.ssl.*

class SSLConnectionFactory(
    val parent: ConnectionFactory = ConnectionFactory.DEFAULT,
    keyManager: KeyManager = EmptyKeyManager,
    trustManager: TrustManager = TrustManager.TRUST_ALL,
    val sslBufferSize: Int = DEFAULT_BUFFER_SIZE,
) : ConnectionFactory {

    private val sslContext: SSLContext by lazy {
        SSLContext.getInstance(SSLMethod.TLSv1_2, keyManager, trustManager)
    }

    override suspend fun connect(
        networkManager: NetworkManager,
        schema: String,
        host: String,
        port: Int,
    ): AsyncChannel {
        val channel = parent.connect(
            networkManager = networkManager,
            schema = schema,
            host = host,
            port = port,
        )

        return connect(
            channel = channel,
            schema = schema,
            host = host,
            port = port
        )
    }

    override suspend fun connect(channel: AsyncChannel, schema: String, host: String, port: Int): AsyncChannel {
        val sslSession = sslContext.clientSession(host = host, port = port)
        return sslSession.asyncChannel(channel, closeParent = true, bufferSize = sslBufferSize)
    }
}
