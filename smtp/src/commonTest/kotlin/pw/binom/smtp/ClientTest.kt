package pw.binom.smtp

import kotlinx.coroutines.runBlocking
import pw.binom.io.ByteBuffer
import pw.binom.io.use
import pw.binom.network.NetworkAddress
import pw.binom.network.NetworkCoroutineDispatcherImpl
import pw.binom.ssl.KeyManager
import pw.binom.ssl.PrivateKey
import pw.binom.ssl.TrustManager
import pw.binom.ssl.X509Certificate
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
    fun test() = runBlocking {

        val client = SMTPClient.tls(
            dispatcher = NetworkCoroutineDispatcherImpl(),
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
}
