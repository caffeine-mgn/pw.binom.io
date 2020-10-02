package pw.binom.io.examples.httpServer

import pw.binom.*
import pw.binom.io.file.AccessType
import pw.binom.io.file.File
import pw.binom.io.file.channel
import pw.binom.io.http.Headers
import pw.binom.io.httpServer.Handler
import pw.binom.io.httpServer.HttpRequest
import pw.binom.io.httpServer.HttpResponse
import pw.binom.io.httpServer.HttpServer
import pw.binom.io.socket.nio.SocketNIOManager
import pw.binom.io.use

fun main() {
    println("Environment.workDirectory: ${Environment.workDirectory}")
    val byteDataPool = ByteBufferPool(10)
    val nioManager = SocketNIOManager()
    val server = HttpServer(nioManager, object : Handler {
        override suspend fun request(req: HttpRequest, resp: HttpResponse) {
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
    server.bindHTTP(port = 8080)
    while (true) {
        nioManager.update()
    }
}