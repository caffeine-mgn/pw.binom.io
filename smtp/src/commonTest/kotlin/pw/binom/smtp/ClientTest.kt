package pw.binom.smtp

import pw.binom.async
import pw.binom.network.NetworkAddress
import pw.binom.network.NetworkDispatcher
import pw.binom.ssl.KeyManager
import pw.binom.ssl.PrivateKey
import pw.binom.ssl.TrustManager
import pw.binom.ssl.X509Certificate
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
                val client = Client.tls(
                    dispatcher = nd,
                    address = NetworkAddress.Immutable("smtp.yandex.ru", 465),
                    keyManager = EmptyKeyManager,
                    trustManager = TrustManager.TRUST_ALL,
                    fromEmail = "git@tlsys.org",
                    login = "git@tlsys.org",
                    password = "8k22thg2O9eKRJz7"
                )
                client.send(
                    to = "caffeine.mgn@gmail.com",
                    from = "git@tlsys.org",
                    body = "From: Drozd <git@tlsys.org>\r\nTo: Drol <caffeine.mgn@gmail.com>\r\nSubject: Hello\r\n" +
                            "Mime-Version: 1.0;\r\n" +
                            "Content-Type: text/html; charset=\"UTF-8\";\r\n\r\n" +
                            "<html>This is test message from <b>Kotlin</b></html>"
                )
                client.asyncClose()
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