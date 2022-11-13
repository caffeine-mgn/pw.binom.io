package pw.binom.io.httpClient

import kotlinx.coroutines.test.runTest
import pw.binom.io.ByteBuffer
import pw.binom.io.http.useBasicAuth
import pw.binom.io.nextBytes
import pw.binom.io.readText
import pw.binom.io.use
import pw.binom.net.toURL
import pw.binom.nextUuid
import pw.binom.thread.Thread
import kotlin.random.Random
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class DownloadTest {

    @Test
    fun getPostTest() = runTest(dispatchTimeoutMs = 10_000) {
        Thread.sleep(1000)
        val filePath = HTTP_STORAGE_URL.appendPath(Random.nextUuid().toString())
        ByteBuffer.alloc(512).use { stubData ->
            Random.nextBytes(stubData)
            stubData.clear()
            HttpClient.create().use { client ->
                client.connect(method = "PUT", uri = filePath).use { req ->
                    req.headers.useBasicAuth(login = "root", password = "root")
                    req.writeData(stubData).use {
                        assertEquals(201, it.responseCode)
                    }
                }
                client.connect(method = "GET", uri = filePath).use { req ->
                    req.headers.useBasicAuth(login = "root", password = "root")
                    req.getResponse().use { resp ->
                        stubData.clear()
                        assertEquals(200, resp.responseCode)
                        assertContentEquals(
                            expected = stubData.toByteArray(),
                            actual = resp.readDataToByteArray()
                        )
                    }
                }
            }
        }
    }

    @Test
    fun test() = runTest(dispatchTimeoutMs = 10_000) {
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
    fun test2() = runTest(dispatchTimeoutMs = 10_000) {
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
