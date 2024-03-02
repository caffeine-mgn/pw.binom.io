package pw.binom.io.httpClient

import kotlinx.coroutines.test.runTest
import pw.binom.io.use
import pw.binom.url.toURL
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.minutes

class HttpProxyTest {
  private val proxy_url = "http://127.0.0.1:8888".toURL()

  @Test
  fun test() =
    runTest(timeout = 20.minutes) {
      val client =
        HttpClient.create(
//            proxyURL = proxy_url,
        )

      client.connect("GET", "https://google.com/".toURL()).use {
        it.getResponse().use { it.readText { it.readText() } }
      }
//        Thread.sleep(10000)
      assertEquals(1, client.connectionPool.connections.size)
      client.connect("GET", "https://google.com/".toURL()).use {
        it.getResponse().use { it.readText { it.readText() } }
      }
      assertEquals(1, client.connectionPool.connections.size)
      return@runTest
    }
}
