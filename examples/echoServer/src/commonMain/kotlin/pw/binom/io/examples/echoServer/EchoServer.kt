package pw.binom.io.examples.echoServer

import pw.binom.ByteBuffer
import pw.binom.io.socket.RawServerSocketChannel
import pw.binom.io.socket.RawSocketChannel
import pw.binom.io.socket.SocketClosedException
import pw.binom.io.socket.SocketSelector

fun main(args: Array<String>) {
    val server = RawServerSocketChannel()
    val selector = SocketSelector()

    server.blocking = false
    server.bind("0.0.0.0", 8899)
    selector.reg(server)
    println("Start listen port 8899")
    val buffer = ByteBuffer.alloc(256)
    while (true) {
        selector.process {
            if (it.channel === server) {
                val client = server.accept()!!
                client.blocking = false
                selector.reg(client).listen(true, false)
                println("Client connected")
            } else {
                try {
                    val client = it.channel as RawSocketChannel
                    buffer.clear()
                    println("Try read ${buffer.remaining}")
                    val len = client.read(buffer)
                    println("Read $len")
                    buffer.flip()
                    client.write(buffer)
                    println("read $len bytes")
                } catch (e: SocketClosedException) {
                    it.cancel()
                    println("Client disconnected")
                }
            }
        }
    }
}