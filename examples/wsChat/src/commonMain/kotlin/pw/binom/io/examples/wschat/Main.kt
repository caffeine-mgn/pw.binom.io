package pw.binom.io.examples.wschat

import pw.binom.io.httpServer.HttpServer
import pw.binom.io.socket.nio.SocketNIOManager
import pw.binom.process.Signal
import kotlin.random.Random

fun main(args: Array<String>) {
    val port = 8083//Random.nextInt(3000, Short.MAX_VALUE.toInt() - 1)
    println("Staring HTTP server on 0.0.0.0:$port")
    val manager = SocketNIOManager()
    val server = HttpServer(manager, ChatHandler())

    server.bindHTTP(port = port)
    while (!Signal.isInterrupted) {
        manager.update(1000)
    }
    server.close()
    manager.close()
}