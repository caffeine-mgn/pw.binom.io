package pw.binom.io.httpServer

import pw.binom.AsyncInput
import pw.binom.AsyncOutput
import pw.binom.io.readln
import pw.binom.io.socket.SocketClosedException
import pw.binom.io.utf8Reader
import pw.binom.pool.DefaultPool

private const val PREFIX = "ConnectionProcessing: "

@OptIn(ExperimentalUnsignedTypes::class)
internal object ConnectionProcessing {

    suspend fun process(
//            connection: SocketNIOManager.ConnectionRaw,
            inputBuffered: AsyncInput,
            outputBuffered: AsyncOutput,
            httpRequestPool: DefaultPool<HttpRequestImpl2>,
            httpResponsePool: DefaultPool<HttpResponseImpl2>,
            handler: Handler,
            allowZlib: Boolean
    ): Boolean {
        val outputBufferid = outputBuffered//connection.bufferedOutput()
//        val buf = AsyncBufferedInput(connection)
        val reader = inputBuffered.utf8Reader()
        val request = reader.readln()!!// ?: return false
        val items = request.split(' ')

        val req = httpRequestPool.borrow()
        req.init(
                method = items[0],
                uri = items.getOrNull(1) ?: "",
                input = inputBuffered,
                allowZlib = allowZlib,
                output = outputBuffered
        )

        val resp = httpResponsePool.borrow()
        var keepAlive: Boolean
        resp.init(
                encode = req.encode,
                keepAlive = req.keepAlive,
                output = outputBufferid
        )
        var closed = false
        try {
            handler.request(req, resp)
        } catch (e: SocketClosedException) {
            closed = true
        } finally {
            keepAlive = resp.keepAlive && !closed
            (resp.body ?: resp.complete()).let {
                it.flush()
                it.close()
            }
            req.flush()
            resp.responseBodyPool.recycle(resp.body!!)
            httpRequestPool.recycle(req)
            httpResponsePool.recycle(resp)
        }
        return keepAlive
    }
}