package pw.binom.io.examples.echoServer

import pw.binom.io.socket.*

fun main(args: Array<String>) {
    val server = ServerSocketChannel()
    val selector = SocketSelector(100)

    server.blocking = false
    server.bind(8899)
    selector.reg(server)
    println("Start listen port 8899")
    val buffer = ByteArray(256)
    while (true) {
        selector.process {
            if (it.channel === server) {
                val client = server.accept()!!
                client.blocking = false
                selector.reg(client)
                println("Client connected")
            } else {
                try {
                    val client = it.channel as SocketChannel
                    val len = client.read(buffer)
                    client.write(buffer, 0, len)
                    println("read $len bytes")
                } catch (e: SocketClosedException) {
                    it.cancel()
                    println("Client disconnected")
                }
            }
        }
    }
}