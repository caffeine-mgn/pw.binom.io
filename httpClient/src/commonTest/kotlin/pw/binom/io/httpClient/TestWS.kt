package pw.binom.io.httpClient

import pw.binom.async
import pw.binom.io.http.websocket.MessageType
import pw.binom.io.readText
import pw.binom.io.use
import pw.binom.io.utf8Appendable
import pw.binom.io.utf8Reader
import pw.binom.network.NetworkDispatcher
import kotlin.test.Ignore
import kotlin.test.Test

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
    @Ignore
    @Test
    fun serverTest() {
        var done = false
        val manager = NetworkDispatcher()
        async {
            try {
                val client = AsyncHttpClient(manager)
                val wsClient = client.request("GET", "ws://127.0.0.1:8080/".toURIOrNull()!!)
                    .websocket("http://127.0.0.1:8080")

                while (true) {
                    val msg = wsClient.read().use {
                        it.utf8Reader().readText()
                    }
                    if (msg.trim() == "exit")
                        break
                    println("Read [$msg]. Send response")
                    wsClient.write(MessageType.BINARY).use {
                        it.utf8Appendable().append("Echo $msg")
                    }
                }

                wsClient.asyncClose()
            } catch (e: Throwable) {
                e.printStackTrace()
            } finally {
                done = true
            }
        }

        while (!done) {
            manager.select(1000)
        }
    }
}