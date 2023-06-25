package pw.binom.io.httpClient

import kotlinx.coroutines.test.runTest
import pw.binom.io.*
import pw.binom.io.http.websocket.MessageType
import pw.binom.url.toURL
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TestWS {
    /*
        @Test
        fun FFF2() {
            val manager = NetworkDispatcher()
            val client = AsyncHttpClient(manager)
            val vv = manager.async {
                val client = client.request(
                    "PATCH",
                    URL("https://images.binom.pw:443/v2/test2/test/blobs/uploads/432ae7a9-dfab-4e70-abd2-1c17502fe1f8")
                )
                val s =
                    File("C:\\TEMP\\8\\test2\\test\\blobs\\6b4e6b132496b605fa0be86f48eac7450a74ecc43809cbe1c8834b71f9e8747f").read()
                        .use {
                            val s = client.upload()
                            it.copyTo(s)
                            s
                        }.response()
            }
            while (true) {
                if (vv.isDone) {
                    break
                }
                manager.select(1000)
            }

            if (vv.isFailure) {
                throw vv.exceptionOrNull!!
            }
        }

        @Ignore
        @Test
        fun FFF() {
            val manager = NetworkDispatcher()
            val client = AsyncHttpClient(manager)
            val vv = manager.async {
                val client = client.request(
                    "PATCH",
                    URL("https://images.binom.pw:443/v2/test2/test/blobs/uploads/432ae7a9-dfab-4e70-abd2-1c17502fe1f8")
                )
                val s =
                    File("C:\\TEMP\\8\\test2\\test\\blobs\\6b4e6b132496b605fa0be86f48eac7450a74ecc43809cbe1c8834b71f9e8747f").read()
                        .use {
                            val s = client.upload()
                            it.copyTo(s)
                            s
                        }.response()
            }
            while (true) {
                if (vv.isDone) {
                    break
                }
                manager.select(1000)
            }

            if (vv.isFailure) {
                throw vv.exceptionOrNull!!
            }
        }
    */

//    object WSEchoServer : TestContainer(
//        image = "jmalloc/echo-server",
//        ports = listOf(
//            Port(internalPort = 8080)
//        ),
//        reuse = true,
//    ) {
//        val externalPort
//            get() = ports[0].externalPort
//    }

    @Test
    fun test() = runTest {
        val message = "Hello world"
        val client = HttpClient.create()
        val wsClient = client.connectWebSocket(
            uri = "ws://127.0.0.1:7142/".toURL(),
        ).start()
        println("Try read first message...")
//        val msg = wsClient.read().use { message ->
//            println("Reading message...")
//            message.bufferedReader().readText()
//        }
//        assertTrue(msg.startsWith("Request served by"))

        wsClient.write(MessageType.BINARY).use {
            message.encodeToByteArray().wrap { msg -> it.write(msg) }
        }
        val echo = wsClient.read().use { message ->
            message.bufferedReader().readText()
        }
        assertEquals(message, echo)
    }

    @Ignore
    @Test
    fun serverTest() = runTest {
        try {
            val client = HttpClient.create()
            val wsClient = client.connectWebSocket("ws://127.0.0.1:8080/".toURL())
                .start()

            while (true) {
                val msg = wsClient.read().use {
                    it.utf8Reader().readText()
                }
                if (msg.trim() == "exit") {
                    break
                }
                println("Read [$msg]. Send response")
                wsClient.write(MessageType.BINARY).use {
                    it.utf8Appendable().append("Echo $msg")
                }
            }

            wsClient.asyncClose()
        } catch (e: Throwable) {
            e.printStackTrace()
            throw e
        }
    }
}
