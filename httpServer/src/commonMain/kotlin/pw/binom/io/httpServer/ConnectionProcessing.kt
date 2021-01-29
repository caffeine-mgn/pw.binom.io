package pw.binom.io.httpServer

import pw.binom.io.AsyncBufferedAsciiInputReader
import pw.binom.network.SocketClosedException
import pw.binom.network.TcpConnection
import pw.binom.pool.DefaultPool

private const val PREFIX = "ConnectionProcessing: "

@OptIn(ExperimentalUnsignedTypes::class)
internal object ConnectionProcessing {

    suspend fun process(
        rawConnection: TcpConnection,
        //inputBuffered: PooledAsyncBufferedInput,
        outputBuffered: PoolAsyncBufferedOutput,
        asciiInputReader: AsyncBufferedAsciiInputReader,
        httpRequestPool: DefaultPool<HttpRequestImpl2>,
        httpResponsePool: DefaultPool<HttpResponseImpl2>,
        handler: Handler,
        allowZlib: Boolean
    ): Boolean {
        val outputBufferid = outputBuffered//connection.bufferedOutput()
//        val buf = AsyncBufferedInput(connection)
//        val reader = inputBuffered.utf8Reader()
        val request = asciiInputReader.readln()!!// ?: return false
        val items = request.split(' ', limit = 3)

        val req = httpRequestPool.borrow()
        req.init(
            method = items[0],
            uri = items.getOrNull(1) ?: "",
            input = asciiInputReader,
            allowZlib = allowZlib,
            output = outputBuffered,
            rawConnection = rawConnection
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
                it.asyncClose()
            }
            req.flush()
            resp.responseBodyPool.recycle(resp.body!!)
            httpRequestPool.recycle(req)
            httpResponsePool.recycle(resp)
        }
        return keepAlive
    }
}