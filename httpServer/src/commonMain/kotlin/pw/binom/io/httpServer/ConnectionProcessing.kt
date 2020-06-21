package pw.binom.io.httpServer

import pw.binom.io.AsyncBufferedInput
import pw.binom.io.readln
import pw.binom.io.socket.SocketClosedException
import pw.binom.io.socket.nio.SocketNIOManager
import pw.binom.io.utf8Reader
import pw.binom.pool.DefaultPool
private const val PREFIX="ConnectionProcessing: "
@OptIn(ExperimentalUnsignedTypes::class)
internal object ConnectionProcessing {

    suspend fun process(
            connection: SocketNIOManager.ConnectionRaw,
            inputBufferPool: DefaultPool<NoCloseInput>,
            httpRequestPool: DefaultPool<HttpRequestImpl2>,
            httpResponsePool: DefaultPool<HttpResponseImpl2>,
            handler: Handler): Boolean {
        println("${PREFIX}Start connection processing...")
//        val buf = AsyncBufferedInput(connection)
        val reader = connection.utf8Reader()
        println("${PREFIX}Read request")
        val request = reader.readln()!!
        println("${PREFIX}Request Readed")
        val items = request.split(' ')

        val req = httpRequestPool.borrow()
        println("Read headers...")
        req.init(
                method = items[0],
                uri = items.getOrNull(1) ?: "",
                input = connection,
                inputBufferPool = inputBufferPool
        )
        println("Header readed")

        val resp = httpResponsePool.borrow()
        var keepAlive: Boolean
        resp.init(
                encode = req.encode,
                keepAlive = req.keepAlive,
                output = connection
        )
        var closed = false
        try {
            handler.request(req, resp)
        } catch (e: SocketClosedException) {
            closed = true
        } finally {
            keepAlive = resp.keepAlive && !closed
            if (resp.body == null) {
                resp.complete()
            } else {
                resp.body!!.flush()
                resp.body!!.close()
            }
            req.flush(inputBufferPool)
            resp.responseBodyPool.recycle(resp.body!!)
            httpRequestPool.recycle(req)
            httpResponsePool.recycle(resp)
        }
        return keepAlive
    }
}