package pw.binom.http.client

import pw.binom.DEFAULT_BUFFER_SIZE
import pw.binom.io.*
import pw.binom.io.http.*
import pw.binom.io.httpClient.protocol.v11.Http11
import pw.binom.io.httpClient.protocol.v11.Http11ConnectFactory2
import pw.binom.io.socket.SocketAddress

suspend fun AsyncChannel.tcpConnectViaHttpProxy(
    address: SocketAddress,
    readBufferSize: Int = DEFAULT_BUFFER_SIZE,
    auth: HttpAuth? = null,
    headers: Headers = emptyHeaders(),
): AsyncChannel {
    val request = "${address.host}:${address.port}"
    val headersForSend = HashHeaders2()
    headersForSend[Headers.HOST] = request
    if (auth != null) {
        headersForSend[Headers.PROXY_AUTHORIZATION] = auth.headerValue
    }
    headersForSend += headers
    bufferedAsciiWriter(closeParent = false).useAsync { writer ->
        Http11.sendRequest(
            output = writer,
            method = "CONNECT",
            request = request,
            headers = headersOf(Headers.HOST to request),
        )
    }
    val reader = bufferedAsciiReader(bufferSize = readBufferSize)
    val resp = try {
        Http11ConnectFactory2.readResponse(reader)
    } catch (e: Throwable) {
        reader.asyncCloseAnyway()
        throw e
    }
    if (resp.responseCode != 200) {
        reader.asyncClose()
        asyncClose()
        throw IOException("Can't connect via http proxy: invalid response ${resp.responseCode}")
    }
    return AsyncChannel.create(
        input = reader,
        output = this@tcpConnectViaHttpProxy,
    ) {
        reader.asyncClose()
        this@tcpConnectViaHttpProxy.asyncClose()
    }
}
