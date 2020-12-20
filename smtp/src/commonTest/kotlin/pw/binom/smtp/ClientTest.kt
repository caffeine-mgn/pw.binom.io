package pw.binom.smtp

import pw.binom.ByteBuffer
import pw.binom.async
import pw.binom.io.use
import pw.binom.network.NetworkAddress
import pw.binom.network.NetworkDispatcher
import pw.binom.ssl.KeyManager
import pw.binom.ssl.PrivateKey
import pw.binom.ssl.TrustManager
import pw.binom.ssl.X509Certificate
import pw.binom.wrap
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
                val client = SMTPClient.tls(
                    dispatcher = nd,
                    address = NetworkAddress.Immutable("smtp.yandex.ru", 465),
                    keyManager = EmptyKeyManager,
                    trustManager = TrustManager.TRUST_ALL,
                    fromEmail = "git@tlsys.org",
                    login = "git@tlsys.org",
                    password = "8k22thg2O9eKRJz7"
                )
                client.multipart(
                    from = "git@tlsys.org",
                    fromAlias = "TradeLine GIT",
                    to = "caffeine.mgn@gmail.com",
                    toAlias = "Anton",
                    subject = "Test Message"
                ) {
                    it.appendText("text/html").use {
                        it.append("<html>Привет из <b>Kotln</b><br><br><i>Это</i> пример HTML с вложением!<br><s>Зачёрктнутый</s>")
                    }

                    it.attach(name = "my_text.txt").use {
                        it.write(ByteBuffer.wrap("MyData in TXT file".encodeToByteArray()))
                    }
                }

                client.multipart(
                    from = "git@tlsys.org",
                    fromAlias = "TradeLine GIT",
                    to = "caffeine.mgn@gmail.com",
                    toAlias = "Anton",
                    subject = "Test Message"
                ) {
                    it.appendText("text/html").use {
                        it.append("<html><s>Второе письмо!</s>")
                    }
                }

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