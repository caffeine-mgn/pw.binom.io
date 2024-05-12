package pw.binom.smtp

import kotlinx.coroutines.runBlocking
import pw.binom.io.socket.InetSocketAddress
import pw.binom.io.useAsync
import pw.binom.io.wrap
import pw.binom.network.NetworkCoroutineDispatcherImpl
import pw.binom.ssl.EmptyKeyManager
import pw.binom.ssl.TrustManager
import kotlin.test.Ignore
import kotlin.test.Test

class ClientTest {
  @Ignore
  @Test
  fun test2() =
    runBlocking {
      val client =
        SMTPClient.tcp(
          dispatcher = NetworkCoroutineDispatcherImpl(),
          address = InetSocketAddress.resolve("127.0.0.1", 1025),
          fromEmail = "test@test.org",
          login = "test@test.org",
          password = "test_password",
        )
      client.multipart(
        from = "test@test.org",
        fromAlias = "Test Binom Client",
        to = "test2@test.org",
        toAlias = "Anton",
        subject = "Test Message",
      ) {
        it.appendText("text/html").useAsync {
          it.append("<html>Hello from <b>Kotln</b><br><br><i>This</i> is an example HTML with attachment!<br><s>Зачёрктнутый</s>")
        }

        it.attach(name = "my_text.txt").useAsync {
          it.write("MyData in TXT file".encodeToByteArray().wrap())
        }
      }

      client.multipart(
        from = "git@tlsys.org",
        fromAlias = "TradeLine GIT",
        to = "caffeine.mgn@gmail.com",
        toAlias = "Anton",
        subject = "Test Message",
      ) {
        it.appendText("text/html").useAsync {
          it.append("<html><s>Second email! Without attachment!</s>")
        }
      }
      client.asyncClose()
    }

  @Ignore
  @Test
  fun test() =
    runBlocking {
      val client =
        SMTPClient.tls(
          dispatcher = NetworkCoroutineDispatcherImpl(),
          address = InetSocketAddress.resolve("smtp.yandex.ru", 465),
          keyManager = EmptyKeyManager,
          trustManager = TrustManager.TRUST_ALL,
          fromEmail = "test@test.org",
          login = "test@test.org",
          password = "test_password",
        )
      client.multipart(
        from = "test@test.org",
        fromAlias = "Test Binom Client",
        to = "test2@test.org",
        toAlias = "Anton",
        subject = "Test Message",
      ) {
        it.appendText("text/html").useAsync {
          it.append("<html>Hello from <b>Kotln</b><br><br><i>This</i> is an example HTML with attachment!<br><s>Зачёрктнутый</s>")
        }

        it.attach(name = "my_text.txt").useAsync {
          it.write("MyData in TXT file".encodeToByteArray().wrap())
        }
      }

      client.multipart(
        from = "git@tlsys.org",
        fromAlias = "TradeLine GIT",
        to = "caffeine.mgn@gmail.com",
        toAlias = "Anton",
        subject = "Test Message",
      ) {
        it.appendText("text/html").useAsync {
          it.append("<html><s>Second email! Without attachment!</s>")
        }
      }
      client.asyncClose()
    }
}
