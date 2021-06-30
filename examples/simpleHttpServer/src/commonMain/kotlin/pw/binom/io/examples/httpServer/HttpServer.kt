package pw.binom.io.examples.httpServer

import pw.binom.*
import pw.binom.io.file.AccessType
import pw.binom.io.file.File
import pw.binom.io.file.channel
import pw.binom.io.file.relative
import pw.binom.io.httpServer.*
import pw.binom.io.use
import pw.binom.network.NetworkAddress
import pw.binom.network.NetworkDispatcher

fun main() {
    println("Environment.workDirectory: ${Environment.workDirectory}")
    val nioManager = NetworkDispatcher()
    val server = HttpServer(nioManager, object : Handler {
        override suspend fun request(req: HttpRequest) {
            val file = File(Environment.workDirectory).relative(req.request)
            if (!file.isFile) {
                req.response { it.status = 404 }
            } else {
                req.response { resp ->
                    resp.headers.contentType = "application/octet-stream"
                    resp.headers.contentLength = file.size.toULong()
                    resp.status = 200
                    file.channel(AccessType.READ).use { channel ->
                        resp.startWriteBinary().use { out ->
                            channel.copyTo(out)
                        }
                    }
                }
            }
        }
    })
    server.bindHttp(NetworkAddress.Immutable(port = 8080))
    while (true) {
        nioManager.select()
    }
}