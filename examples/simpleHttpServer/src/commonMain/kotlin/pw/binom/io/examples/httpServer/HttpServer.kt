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
import pw.binom.io.socket.nio.SingleThreadNioManager
import pw.binom.io.use
import pw.binom.pool.ObjectPool

suspend fun Input.copyTo(output: AsyncOutput, pool: ObjectPool<ByteBuffer>) {
    val buf = pool.borrow()
    while (true) {
        buf.clear()
        val s = read(buf)
        if (s == 0)
            break
        buf.flip()
        output.write(buf)
    }
}

fun main(args: Array<String>) {
    println("Environment.workDirectory: ${Environment.workDirectory}")
    val byteDataPool = ByteBufferPool()
    val nioManager = SingleThreadNioManager()
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