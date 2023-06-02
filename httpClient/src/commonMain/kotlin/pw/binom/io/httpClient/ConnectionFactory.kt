package pw.binom.io.httpClient

import pw.binom.io.AsyncChannel
import pw.binom.io.AsyncWriter
import pw.binom.io.http.Headers
import pw.binom.io.http.Utils
import pw.binom.io.http.forEachHeader
import pw.binom.io.socket.NetworkAddress
import pw.binom.network.NetworkManager
import pw.binom.network.tcpConnect
import pw.binom.url.URL

interface ConnectionFactory {
    companion object {
        val DEFAULT: ConnectionFactory = object : ConnectionFactory {
            override suspend fun connect(
                networkManager: NetworkManager,
                schema: String,
                host: String,
                port: Int,
            ): AsyncChannel = networkManager.tcpConnect(
                NetworkAddress.create(
                    host = host,
                    port = port,
                ),
            )

            override suspend fun writeRequest(
                output: AsyncWriter,
                method: String,
                headers: Headers,
                url: URL,
                request: String,
            ) {
                output.append(method).append(" ").append(request).append(" ").append("HTTP/1.1").append(Utils.CRLF)
                headers.forEachHeader { key, value ->
                    output.append(key).append(": ").append(value).append(Utils.CRLF)
                }
                output.append(Utils.CRLF)
            }
        }
    }

    suspend fun connect(networkManager: NetworkManager, schema: String, host: String, port: Int): AsyncChannel
    suspend fun writeRequest(
        output: AsyncWriter,
        method: String,
        headers: Headers,
        url: URL,
        request: String = url.request.ifEmpty { "/" },
    )
}
