package pw.binom.smtp

import pw.binom.ByteBuffer
import pw.binom.async2
import pw.binom.concurrency.joinAndGetOrThrow
import pw.binom.io.use
import pw.binom.network.NetworkAddress
import pw.binom.network.NetworkDispatcher
import pw.binom.ssl.KeyManager
import pw.binom.ssl.PrivateKey
import pw.binom.ssl.TrustManager
import pw.binom.ssl.X509Certificate
import pw.binom.wrap
import kotlin.test.Ignore
import kotlin.test.Test

object EmptyKeyManager : KeyManager {
    override fun getPrivate(serverName: String?): PrivateKey? = null

    override fun getPublic(serverName: String?): X509Certificate? = null

    override fun close() {
    }
}

class ClientTest {

    @Ignore
    @Test
    fun test() {
        val networkDispatcher = NetworkDispatcher()

        val feature = networkDispatcher.startCoroutine {
            val client = SMTPClient.tls(
                dispatcher = networkDispatcher,
                address = NetworkAddress.Immutable("smtp.yandex.ru", 465),
                keyManager = EmptyKeyManager,
                trustManager = TrustManager.TRUST_ALL,
                fromEmail = "test@test.org",
                login = "test@test.org",
                password = "test_password"
            )
            client.multipart(
                    from = "test@test.org",
                    fromAlias = "Test Binom Client",
                    to = "test2@test.org",
                    toAlias = "Anton",
                    subject = "Test Message"
                ) {
                    it.appendText("text/html").use {
                        it.append("<html>Hello from <b>Kotln</b><br><br><i>This</i> is an example HTML with attachment!<br><s>Зачёрктнутый</s>")
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
                        it.append("<html><s>Second email! Without attachment!</s>")
                    }
                }

                client.asyncClose()
        }

        while (!feature.isDone) {
            networkDispatcher.select()
        }
        feature.joinAndGetOrThrow()
    }
}