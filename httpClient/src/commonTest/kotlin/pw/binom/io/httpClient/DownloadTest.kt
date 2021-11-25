package pw.binom.io.httpClient

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import pw.binom.io.readText
import pw.binom.io.use
import pw.binom.net.toURI
import pw.binom.network.Network
import kotlin.test.Test

class DownloadTest {

    @Test
    fun test() =
        runBlocking {
            HttpClient.create1(networkDispatcher = Dispatchers.Network).use { client ->
                client.connect(
                    method = "GET",
                    uri = "https://example.com".toURI(),
                ).use { query->
                    val txt = query.getResponse().readText().use { it.readText() }
                    println("txt: $txt")
                }
            }
        }
}