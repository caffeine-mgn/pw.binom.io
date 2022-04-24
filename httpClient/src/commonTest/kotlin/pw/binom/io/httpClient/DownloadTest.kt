package pw.binom.io.httpClient

import kotlinx.coroutines.test.runTest
import pw.binom.io.readText
import pw.binom.io.use
import pw.binom.net.toURL
import kotlin.test.Ignore
import kotlin.test.Test

class DownloadTest {

    @Test
    fun test() = runTest {
        HttpClient.create().use { client ->
            client.connect(
                method = "GET",
                uri = "https://www.ntv.ru/".toURL(),
            ).use { query ->
                val txt = query.getResponse().readText().use { it.readText() }
                println("txt: $txt")
            }
        }
    }

    @Ignore
    @Test
    fun test2() = runTest {
        HttpClient.create().use { client ->
            client.connect(
                method = "GET",
                uri = "http://127.0.0.1:2375/".toURL(),
            ).use { query ->
                val txt = query.getResponse().readText().use { it.readText() }
                println("txt: $txt")
            }
        }
    }
}
