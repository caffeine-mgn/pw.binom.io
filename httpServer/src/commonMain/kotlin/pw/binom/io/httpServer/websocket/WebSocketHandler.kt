package pw.binom.io.httpServer.websocket

// @Deprecated("Not use it")
// abstract class WebSocketHandler : Handler3Deprecated {
//
//    interface ConnectRequest {
//        val uri: String
//        val contextUri: String
//        val headers: Map<String, List<String>>
//        suspend fun accept(headers: Map<String, List<String>> = emptyMap()): WebSocketConnection
//        suspend fun reject()
//    }
//
//    private class RejectedException : RuntimeException()
//
//    private val sha1 = Sha1MessageDigest()
//
//    private inner class ConnectRequestImpl(
//        val key: String,
//        val resp: HttpResponseDeprecated,
//        val rawInput: AsyncInput,
//        val rawOutout: AsyncOutput,
//        val rawConnection: TcpConnection,
//        override val uri: String,
//        override val contextUri: String, override val headers: Map<String, List<String>>
//    ) : ConnectRequest {
//        var currentConnection: WebSocketConnection? = null
//
//        private var resumed = false
//        override suspend fun accept(headers: Map<String, List<String>>): WebSocketConnection {
//            require(!resumed) { "Request already resumed" }
//            resumed = true
//            resp.status = 101
//
//            resp.addHeader(Headers.CONNECTION, Headers.UPGRADE)
//            resp.addHeader(Headers.UPGRADE, Headers.WEBSOCKET)
//            resp.addHeader(Headers.SEC_WEBSOCKET_ACCEPT, HandshakeSecret.generateResponse(sha1, key))
//
//            headers.forEach { k ->
//                k.value.forEach {
//                    resp.addHeader(k.key, it)
//                }
//            }
//            resp.complete()
//            val connection = ServerWebSocketConnection(
//                input = rawInput,
//                output = rawOutout,
//                rawConnection = rawConnection
//            )
//            currentConnection = connection
//            return connection
//        }
//
//        override suspend fun reject() {
//            require(!resumed) { "Request already resumed" }
//            resumed = true
//            throw RejectedException()
//        }
//    }
//
//    protected abstract suspend fun connected(request: ConnectRequest)
//
//    override suspend fun request(req: HttpRequestDeprecated, resp: HttpResponseDeprecated) {
//        resp.enableKeepAlive = false
//        if (req.method != "GET") {
//            throw IOException("Invalid Http Request method. Method: [${req.method}], uri: [${req.uri}]")
//        }
//        if (req.headers[Headers.CONNECTION]
//                ?.singleOrNull()
//                ?.splitToSequence(',')
//                ?.map { it.trim() }
//                ?.any { it == Headers.UPGRADE } != true
//            || req.headers[Headers.UPGRADE]?.singleOrNull() != Headers.WEBSOCKET
//        ) {
//            resp.status = 403
//            resp.enableKeepAlive = false
//            resp.complete()
//            return
//        }
//        val key = req.headers["Sec-WebSocket-Key"]?.singleOrNull() ?: TODO()
//        resp.enableKeepAlive = false
//        try {
//            val con = ConnectRequestImpl(
//                key = key,
//                uri = req.uri,
//                contextUri = req.contextUri,
//                resp = resp,
//                rawInput = req.rawInput,
//                rawOutout = req.rawOutput,
//                rawConnection = req.rawConnection,
//                headers = req.headers
//            )
//            connected(con)
// //            if (con.currentConnection?.incomeMessageListener != null) {
// //                suspendCoroutine<Unit> { }
// //            }
//        } catch (e: RejectedException) {
//            e.printStackTrace()
//            resp.status = 403
//            resp.enableKeepAlive = false
//            resp.complete()
//            return
//        }
//    }
// }
