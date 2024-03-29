package pw.binom.io.examples.wschat

import pw.binom.io.httpServer.HttpServer
import pw.binom.network.NetworkAddress
import pw.binom.network.NetworkDispatcher
import pw.binom.process.Signal

fun main(args: Array<String>) {
    val port = 8083//Random.nextInt(3000, Short.MAX_VALUE.toInt() - 1)
    println("Staring HTTP server on 0.0.0.0:$port")
    val manager = NetworkDispatcher()
    val server = HttpServer(manager, ChatHandler())

    server.bindHttp(NetworkAddress.Immutable(port = port))
    while (!Signal.isInterrupted) {
        manager.select(1000)
    }
    manager.close()
}