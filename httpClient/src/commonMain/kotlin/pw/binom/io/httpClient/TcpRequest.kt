package pw.binom.io.httpClient

import pw.binom.io.AsyncChannel
import pw.binom.io.IOException
import pw.binom.io.http.HashHeaders2
import pw.binom.io.http.Headers
import pw.binom.io.http.emptyHeaders
import pw.binom.url.URL

class TcpRequest(val url: URL, val method: String, val client: HttpClient) {
    val headers = HashHeaders2()
    private var started = false
    suspend fun start(): AsyncChannel {
        check(!started) { "Connection already started" }
        started = true
        val request = client.startConnect(method = method, uri = url, headers = headers, keepAlive = false)
        val resp = request.flush()
        if (resp.responseCode != 101) {
            throw IOException("Invalid Response code: ${resp.responseCode}")
        }
        HttpMetrics.defaultHttpRequestCountMetric.dec()
        return AsyncChannel.create(
            input = request.input,
            output = request.output,
        )
    }
}

fun HttpClient.connectTcp(
    uri: URL,
    headers: Headers = emptyHeaders(),
    method: String = "GET",
): TcpRequest {
    val req = TcpRequest(
        url = uri,
        method = method,
        client = this,
    )
    req.headers[Headers.CONNECTION] = Headers.UPGRADE
    req.headers[Headers.UPGRADE] = Headers.TCP
    req.headers[Headers.HOST] = uri.host + (uri.port?.let { ":$it" } ?: "")
    req.headers.add(headers)
    return req
}
