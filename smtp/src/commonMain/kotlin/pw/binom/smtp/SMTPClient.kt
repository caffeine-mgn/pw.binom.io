package pw.binom.smtp

import kotlinx.coroutines.Dispatchers
import pw.binom.io.AsyncCloseable
import pw.binom.io.socket.NetworkAddress
import pw.binom.io.socket.ssl.asyncChannel
import pw.binom.network.Network
import pw.binom.network.NetworkManager
import pw.binom.network.tcpConnect
import pw.binom.ssl.KeyManager
import pw.binom.ssl.SSLContext
import pw.binom.ssl.SSLMethod
import pw.binom.ssl.TrustManager

interface SMTPClient : AsyncCloseable {
    companion object {
        /**
         * Creates SMTP client without TLS
         */
        suspend fun tcp(
            dispatcher: NetworkManager = Dispatchers.Network,
            login: String,
            password: String,
            fromEmail: String,
            address: NetworkAddress
        ): SMTPClient {
            val connect = dispatcher.tcpConnect(address)
            val client = BaseSMTPClient(connect)
            return try {
                client.start(sendFromDomain = fromEmail, login = login, password = password)
                client
            } catch (e: Throwable) {
                client.asyncClose()
                throw e
            }
        }

        /**
         * Creates TLS SMTP Client
         */
        suspend fun tls(
            dispatcher: NetworkManager = Dispatchers.Network,
            login: String,
            password: String,
            fromEmail: String,
            address: NetworkAddress,
            keyManager: KeyManager,
            trustManager: TrustManager,
            tlsHost: String = address.host,
            tlsPort: Int = address.port,
        ): SMTPClient {
            val connect = dispatcher.tcpConnect(address)
            val sslContext = SSLContext.getInstance(SSLMethod.TLSv1_2, keyManager, trustManager)
            val clientSession = sslContext.clientSession(tlsHost, tlsPort)
            val sslConnect = clientSession.asyncChannel(connect)
            val client = BaseSMTPClient(sslConnect)
            return try {
                client.start(sendFromDomain = fromEmail, login = login, password = password)
                client
            } catch (e: Throwable) {
                client.asyncClose()
                throw e
            }
        }
    }

    suspend fun multipart(
        from: String,
        fromAlias: String?,
        to: String,
        toAlias: String?,
        subject: String?,
        msg: suspend (HtmlMultipartMessage) -> Unit
    )
}
