package pw.binom.io.httpClient

import pw.binom.URL
import pw.binom.async
import pw.binom.io.http.websocket.MessageType
import pw.binom.io.readText
import pw.binom.io.use
import pw.binom.io.utf8Appendable
import pw.binom.io.utf8Reader
import pw.binom.network.NetworkDispatcher
import kotlin.test.Test

class TestWS {

    @Test
    fun test() {
        var done = false
        val manager = NetworkDispatcher()
        async {
            try {
                val client = AsyncHttpClient(manager)
                val wsClient = client.request("GET", URL("ws://127.0.0.1:8080/"))
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