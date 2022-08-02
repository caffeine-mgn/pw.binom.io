package pw.binom.io.socket.ssl

import kotlinx.coroutines.runBlocking
import pw.binom.io.bufferedAsciiReader
import pw.binom.network.NetworkAddress
import pw.binom.network.NetworkCoroutineDispatcherImpl
import pw.binom.network.tcpConnect
import pw.binom.ssl.EmptyKeyManager
import pw.binom.ssl.SSLContext
import pw.binom.ssl.SSLMethod
import pw.binom.ssl.TrustManager
import kotlin.test.Ignore
import kotlin.test.Test

// object EmptyKeyManager : KeyManager {
//    override fun getPrivate(serverName: String?): PrivateKey? = null
//
//    override fun getPublic(serverName: String?): X509Certificate? = null
//
//    override fun close() {
//    }
// }

@Ignore
class ClientTest {

    @Test
    fun test() = runBlocking {
        val nd = NetworkCoroutineDispatcherImpl()
        val rawConnection = nd.tcpConnect(NetworkAddress.Immutable("smtp.yandex.ru", 465))
        val sslContext = SSLContext.getInstance(SSLMethod.TLSv1_2, EmptyKeyManager, TrustManager.TRUST_ALL)
        val clientSession = sslContext.clientSession("smtp.yandex.ru", 465)
        val sslConnect = clientSession.asyncChannel(rawConnection)

        val reader = sslConnect.bufferedAsciiReader()
        println("->${reader.readln()}")
    }
}
