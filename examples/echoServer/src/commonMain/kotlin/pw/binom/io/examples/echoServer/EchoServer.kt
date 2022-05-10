package pw.binom.io.examples.echoServer

import pw.binom.io.ByteBuffer
import pw.binom.network.*

fun main(args: Array<String>) {
    val server = TcpServerSocketChannel()
    val selector = Selector.open()

    server.bind(NetworkAddress.Immutable("0.0.0.0", 8899))
    selector.attach(server, 0, server)
    println("Start listen port 8899")
    val buffer = ByteBuffer.alloc(256)
    while (true) {
        selector.select { key, mode ->
            if (key.attachment === server) {
                val client = server.accept()!!
                selector.attach(client, Selector.INPUT_READY, client)
                println("Client connected")
            } else {
                val client = key.attachment as TcpClientSocketChannel
                try {
                    buffer.clear()
                    println("Try read ${buffer.remaining}")
                    val len = client.read(buffer)
                    println("Read $len")
                    buffer.flip()
                    client.write(buffer)
                    println("read $len bytes")
                } catch (e: SocketClosedException) {
                    client.close()
                    key.close()
                    println("Client disconnected")
                }
            }
        }
    }
}