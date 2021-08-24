package pw.binom.io.examples.wschat

import pw.binom.concurrency.Worker
import pw.binom.io.http.websocket.MessageType
import pw.binom.concurrency.create
import pw.binom.concurrency.sleep
import pw.binom.coroutine.Dispatcher
import pw.binom.coroutine.getCurrentDispatcher
import pw.binom.coroutine.start
import pw.binom.io.*
import pw.binom.io.httpServer.Handler
import pw.binom.io.httpServer.HttpRequest

class ChatHandler : Handler {

    val worker = Worker.create()

    override suspend fun request(req: HttpRequest) {
        var networkDispatcher = Dispatcher.getCurrentDispatcher()!!
        val connection = req.acceptWebsocket()
        connection.write(MessageType.TEXT).utf8Appendable().use {
            it.append("Write you message. I will send your message to you with delay 1 sec")
        }
        while (true) {
            connection.read().bufferedReader().use {
                val txt = it.readText()
                worker.start {
                    sleep(1000)
                    networkDispatcher.start {
                        connection.write(MessageType.TEXT).use {
                            it.bufferedWriter().use {
                                it.append("Echo: $txt")
                            }
                        }
                    }
                }
            }
        }
    }
}