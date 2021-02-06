package pw.binom.io.socket.ssl

import pw.binom.async
import pw.binom.io.bufferedAsciiReader
import pw.binom.network.NetworkAddress
import pw.binom.network.NetworkDispatcher
import pw.binom.ssl.*
import kotlin.test.Test

object EmptyKeyManager : KeyManager {
    override fun getPrivate(serverName: String?): PrivateKey? = null

    override fun getPublic(serverName: String?): X509Certificate? = null

    override fun close() {
    }
}

class ClientTest {

    @Test
    fun test() {
        val nd = NetworkDispatcher()

        var done = false

        async {
            try {
                val rawConnection = nd.tcpConnect(NetworkAddress.Immutable("smtp.yandex.ru", 465))
                val sslContext = SSLContext.getInstance(SSLMethod.TLSv1_2, EmptyKeyManager, TrustManager.TRUST_ALL)
                val clientSession = sslContext.clientSession("smtp.yandex.ru", 465)
                val sslConnect = clientSession.asyncChannel(rawConnection)

                val reader = sslConnect.bufferedAsciiReader()
                println("->${reader.readln()}")

            } catch (e: Throwable) {
                e.printStackTrace()
            } finally {
                done = true
            }
        }

        while (!done) {
            nd.select()
        }
    }
}