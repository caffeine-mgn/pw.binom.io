package pw.binom.io.examples.wschat

import pw.binom.io.httpServer.HttpServer
import pw.binom.io.socket.nio.SocketNIOManager
import pw.binom.process.Signal
import kotlin.random.Random

fun main(args: Array<String>) {
    val port = Random.nextInt(3000, Short.MAX_VALUE.toInt() - 1).toShort()
    println("Staring HTTP server on 0.0.0.0:$port")
    val manager = SocketNIOManager()
    val server = HttpServer(manager, ChatHandler())

    var done = false

    server.bindHTTP(port = 8080)
    Signal.listen(Signal.Type.CTRL_C) {
        done = true
    }
    while (!done) {
        manager.update(1000)
    }
    server.close()
    manager.close()
}