package pw.binom.io.examples.echoServer

import pw.binom.io.socket.*

fun main(args: Array<String>) {
    Socket.startup()
    val server = ServerSocketChannel()
    val selector = SocketSelector(100)

    server.blocking = false
    server.bind(8899)
    selector.reg(server)

    val buffer = ByteArray(256)
    while (true) {
        selector.process {
            if (it.channel === server) {
                selector.reg(server.accept()!!)
                println("Client connected")
            } else {
                try {
                    val client = it.channel as SocketChannel
                    val len = client.read(buffer)
                    client.write(buffer, 0, len)
                    println("Readed $len")
                } catch (e: SocketClosedException) {
                    it.cancel()
                    println("Client disconnected")
                }
            }
        }
    }
}