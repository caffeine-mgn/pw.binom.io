package pw.binom.io.httpClient

import pw.binom.io.socket.SocketAddress
import pw.binom.url.URL
import pw.binom.url.toURL

interface RequestHook {
//    suspend fun buildRequest(
//        method: String,
//        url: URL,
//        headers: Headers,
//        output: AsyncBufferedAsciiWriter,
//        connectionFactory: ConnectionFactory,
//        channel: AsyncChannel,
//    ): HttpRequestBody

    suspend fun connectAddress(url: URL): URL

    companion object {
        private fun URL.getDefaultPort() =
            port ?: when (schema) {
                "ws", "http" -> 80
                "wss", "https" -> 443
                "ssh" -> 22
                "ftp" -> 21
                "rdp" -> 3389
                "vnc" -> 5900
                "telnet" -> 23
                else -> throw IllegalArgumentException("Unknown default port for $this")
            }
    }

    object Default : RequestHook {
//        override suspend fun buildRequest(
//            method: String,
//            url: URL,
//            headers: Headers,
//            output: AsyncBufferedAsciiWriter,
//            connectionFactory: ConnectionFactory,
//            channel: AsyncChannel,
//        ): HttpRequestBody {
//            Http11ConnectFactory2.sendRequest(
//                output = output,
//                method = method,
//                request = url.request,
//                headers = headers,
//            )
//        }

        override suspend fun connectAddress(url: URL) = url
    }

    class HttpProxy(val url: SocketAddress/*, val fallback: RequestHook*/) : RequestHook {

//        private suspend fun buildRaw(
//            method: String,
//            url: URL,
//            headers: Headers,
//            output: AsyncBufferedAsciiWriter,
//            connectionFactory: ConnectionFactory,
//            channel: AsyncChannel,
//        ): HttpRequestBody {
//            val connect = connectionFactory.connect(
//                channel = channel,
//                schema = url.schema,
//                host = url.host,
//                port = url.port ?: url.getDefaultPort()
//            )
//            output.append("CONNECT").append(" ").append(url.toString()).append(" ").append("HTTP/1.1")
//                .append(Utils.CRLF)
//            output.append(Headers.HOST).append(": ").append(url.host + (url.port?.let { ":$it" } ?: ""))
//                .append(Utils.CRLF)
//            headers.forEachHeader { key, value ->
//                output.append(key).append(": ").append(value).append(Utils.CRLF)
//            }
//            output.append("Proxy-Connection: Keep-Alive").append(Utils.CRLF).append(Utils.CRLF)
//            fallback.buildRequest(
//                method = method,
//                url = url,
//                output = output,
//                headers = headers,
//                connectionFactory = connectionFactory,
//                channel = connect,
//            )
//        }

//        private suspend fun buildHttpRequest(
//            method: String,
//            url: URL,
//            headers: Headers,
//            output: AsyncBufferedAsciiWriter,
//            connectionFactory: ConnectionFactory,
//            channel: AsyncChannel,
//        ): HttpRequestBody {
//            output.append(method).append(" ").append(url.toString()).append(" ").append("HTTP/1.1").append(Utils.CRLF)
//            headers.forEachHeader { key, value ->
//                output.append(key).append(": ").append(value).append(Utils.CRLF)
//            }
//            output.append("Proxy-Connection: Keep-Alive").append(Utils.CRLF)
//            output.append(Utils.CRLF)
//        }

//        override suspend fun buildRequest(
//            method: String,
//            url: URL,
//            headers: Headers,
//            output: AsyncBufferedAsciiWriter,
//            connectionFactory: ConnectionFactory,
//            channel: AsyncChannel,
//        ) {
//            when (url.schema) {
//                "http" -> buildHttpRequest(
//                    method = method,
//                    url = url,
//                    headers = headers,
//                    output = output,
//                    connectionFactory = connectionFactory,
//                    channel = channel,
//                )
//
//                else -> buildRaw(
//                    method = method,
//                    url = url,
//                    headers = headers,
//                    output = output,
//                    connectionFactory = connectionFactory,
//                    channel = channel,
//
//                    )
//            }
//        }

        override suspend fun connectAddress(url: URL) =
            "http://${url.host}:${url.port ?: 80}".toURL()
//            NetworkAddress.create(
//                host = this.url.host,
//                port = this.url.port ?: url.getDefaultPort()
//            )
    }
}
