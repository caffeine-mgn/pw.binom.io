package pw.binom.io.examples.wschat

import pw.binom.async
import pw.binom.concurrency.asReference
import pw.binom.doFreeze
import pw.binom.io.http.websocket.MessageType
import pw.binom.io.http.websocket.WebSocketClosedException
import pw.binom.io.http.websocket.WebSocketConnection
import pw.binom.io.httpServer.websocket.WebSocketHandler
import pw.binom.io.readText
import pw.binom.io.use
import pw.binom.io.utf8Appendable
import pw.binom.io.utf8Reader
import pw.binom.thread.Worker
import pw.binom.thread.sleep

class ChatHandler : WebSocketHandler() {

    private val clients = HashSet<WebSocketConnection>()
    private val forDelete = ArrayList<WebSocketConnection>()
    val worker = Worker()
//    private suspend fun sendToClients(text: String, filter: (WebSocketConnection) -> Boolean = { true }) {
//
//        clients.asSequence()
//                .filter(filter)
//                .forEach {
//                    try {
//                        it.write(MessageType.TEXT).utf8Appendable().use {
//                            it.append(text)
//                        }
//                    } catch (e: WebSocketClosedException) {
//                        it.close()
//                        forDelete.add(it)
//                    }
//                }
//        forDelete.forEach {
//            clients.remove(it)
//        }
//        forDelete.clear()
//    }

    override suspend fun connected(request: ConnectRequest) {
        try {
            val connection = request.accept()
            var name = request.headers["X-Nick-Name"]?.singleOrNull()
            if (name == null) {
                connection.write(MessageType.TEXT).utf8Appendable().use {
                    it.append("Write you nick name")
                }
                name = connection.read().utf8Reader().use {
                    it.readText().trim()
                }
            }
            println("Client connected $name")
            while (true) {
                connection.read().utf8Reader().use {
                    val txt = it.readText()
                    println("Read: ${txt} ${Worker.current?.id}")

                    worker.execute(connection) {
                        try {
                            println("Sleep... ${Worker.current?.id}")
                            Worker.sleep(5000)
                            println("Try to write... ${Worker.current?.id}")
                            it.write(MessageType.TEXT) {
                                it.utf8Appendable().use {
                                    it.append("Echo!!!")
                                }
                            }
                        } catch (e: Throwable) {
                            e.printStackTrace()
                            throw e
                        }
                    }
                }
            }
        } catch (e:Throwable) {
            e.printStackTrace()
            throw e
        }
    }
}