package pw.binom.io.examples.httpServer

import pw.binom.*
import pw.binom.io.ByteBuffer
import pw.binom.io.file.AccessType
import pw.binom.io.file.File
import pw.binom.io.file.channel
import pw.binom.io.http.Headers
import pw.binom.io.httpServer.Handler
import pw.binom.io.httpServer.HttpRequest
import pw.binom.io.httpServer.HttpResponse
import pw.binom.io.httpServer.HttpServer
import pw.binom.io.socket.nio.SingleThreadNioManager
import pw.binom.io.use
import pw.binom.pool.DefaultPool

fun main(args: Array<String>) {
    val byteDataPool = ByteDataBufferPool()
    val packagePool = DefaultPool(10) { ByteBuffer(DEFAULT_BUFFER_SIZE) }
    val nioManager = SingleThreadNioManager(packagePool, byteDataPool)
    val server = HttpServer(nioManager, object : Handler {
        override suspend fun request(req: HttpRequest, resp: HttpResponse) {
            val buf = byteDataPool.borrow()
            val file = File(File(Environment.workDirectory), req.uri)
            println("Environment.workDirectory: ${Environment.workDirectory}")
            println("URI: ${req.uri}")
            println("Can't find file ${file.path}")
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
    })
    server.bindHTTP(port = 8080)
    while (true) {
        nioManager.update()
    }
}