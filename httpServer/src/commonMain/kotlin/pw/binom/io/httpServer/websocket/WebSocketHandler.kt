package pw.binom.io.httpServer.websocket

import pw.binom.AsyncInput
import pw.binom.AsyncOutput
import pw.binom.io.Sha1
import pw.binom.io.http.Headers
import pw.binom.io.http.websocket.HandshakeSecret
import pw.binom.io.http.websocket.WebSocketConnection
import pw.binom.io.httpServer.Handler
import pw.binom.io.httpServer.HttpRequest
import pw.binom.io.httpServer.HttpResponse
import pw.binom.io.socket.nio.SocketNIOManager
import kotlin.coroutines.suspendCoroutine

abstract class WebSocketHandler : Handler {

    interface ConnectRequest {
        val uri: String
        val contextUri: String
        val headers: Map<String, List<String>>
        suspend fun accept(headers: Map<String, List<String>> = emptyMap()): WebSocketConnection
        suspend fun reject()
    }

    private class RejectedException : RuntimeException()

    private val sha1 = Sha1()

    private inner class ConnectRequestImpl(val key: String,
                                           val resp: HttpResponse,
                                           val rawInput: AsyncInput,
                                           val rawOutout: AsyncOutput,
                                           val rawConnection: SocketNIOManager.ConnectionRaw,
                                           override val uri: String,
                                           override val contextUri: String, override val headers: Map<String, List<String>>) : ConnectRequest {
        private var resumed = false
        override suspend fun accept(headers: Map<String, List<String>>): WebSocketConnection {
            require(!resumed) { "Request already resumed" }
            resumed = true
            resp.status = 101

            resp.addHeader(Headers.CONNECTION, Headers.UPGRADE)
            resp.addHeader(Headers.UPGRADE, Headers.WEBSOCKET)
            resp.addHeader(Headers.SEC_WEBSOCKET_ACCEPT, HandshakeSecret.generateResponse(sha1, key))

            headers.forEach { k ->
                k.value.forEach {
                    resp.addHeader(k.key, it)
                }
            }
            resp.complete()
            return ServerWebSocketConnection(
                    input = rawInput,
                    output = rawOutout,
                    rawConnection = rawConnection
            )
        }

        override suspend fun reject() {
            require(!resumed) { "Request already resumed" }
            resumed = true
            throw RejectedException()
        }
    }

    protected abstract suspend fun connected(request: ConnectRequest)

    override suspend fun request(req: HttpRequest, resp: HttpResponse) {
        if (req.method != "GET")
            TODO()
        if (req.headers[Headers.CONNECTION]
                        ?.singleOrNull()
                        ?.splitToSequence(',')
                        ?.map { it.trim() }
                        ?.any { it == Headers.UPGRADE } != true
                || req.headers[Headers.UPGRADE]?.singleOrNull() != Headers.WEBSOCKET
        ) {
            resp.status = 403
            resp.enableKeepAlive = false
            resp.complete().close()
            return
        }
        val key = req.headers["Sec-WebSocket-Key"]?.singleOrNull() ?: TODO()
        req.headers.forEach { k ->
            k.value.forEach {
                println("${k.key}: $it")
            }
        }

        resp.enableKeepAlive = false
        try {
            connected(ConnectRequestImpl(
                    key = key,
                    uri = req.uri,
                    contextUri = req.contextUri,
                    resp = resp,
                    rawInput = req.rawInput,
                    rawOutout = req.rawOutput,
                    rawConnection = req.rawConnection,
                    headers = req.headers
            ))
            println("suspend!")
            suspendCoroutine<Unit> { }
        } catch (e: RejectedException) {
            resp.status = 403
            resp.enableKeepAlive = false
            resp.complete()
            return
        }
        println("work next")
        /*
        val rawBuffered = req.rawInput.bufferedInput()
        println("Try read...")


        val header = WebSocketHeader()
        WebSocketHeader.read(rawBuffered, header)

        println("Finish: [${header.finishFlag}], Opcode: [${header.opcode}], mask: [${header.maskFlag}], len: [${header.length}]")

        val buf = ByteBuffer.alloc(header.length.toInt())
        rawBuffered.readFully(buf)
        buf.flip()
        buf.forEachIndexed { i, byte ->
            if (header.maskFlag) {
                val b = byte xor header.mask[i and 0x03]
                println("$i->${b}")
            } else {
                println("$i->${byte}")
            }
        }
    } catch (e: Throwable) {
        e.printStacktrace()
    }
    */
    }
}