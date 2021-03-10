package pw.binom.io.examples.httpServer

import pw.binom.*
import pw.binom.io.file.AccessType
import pw.binom.io.file.File
import pw.binom.io.file.channel
import pw.binom.io.http.Headers
import pw.binom.io.httpServer.Handler3Deprecated
import pw.binom.io.httpServer.HttpRequestDeprecated
import pw.binom.io.httpServer.HttpResponseDeprecated
import pw.binom.io.httpServer.HttpServer3
import pw.binom.io.use
import pw.binom.network.NetworkAddress
import pw.binom.network.NetworkDispatcher

fun main() {
    println("Environment.workDirectory: ${Environment.workDirectory}")
    val byteDataPool = ByteBufferPool(10)
    val nioManager = NetworkDispatcher()
    val server = HttpServer3(nioManager, object : Handler3Deprecated {
        override suspend fun request(req: HttpRequestDeprecated, resp: HttpResponseDeprecated) {
            val file = File(File(Environment.workDirectory), req.uri)
            if (!file.isFile) {
                resp.status = 404
                resp.complete()
            } else {
                resp.status = 200
                resp.addHeader(Headers.CONTENT_TYPE, "application/octet-stream")
                resp.addHeader(Headers.CONTENT_LENGTH, file.size.toString())
                file.channel(AccessType.READ).use { channel ->
                    channel.copyTo(resp.complete(), byteDataPool)
                }
            }
        }
    }, outputBufferSize = 1024 * 1024 * 3)
    server.bindHTTP(NetworkAddress.Immutable(port = 8080))
    while (true) {
        nioManager.select()
    }
}