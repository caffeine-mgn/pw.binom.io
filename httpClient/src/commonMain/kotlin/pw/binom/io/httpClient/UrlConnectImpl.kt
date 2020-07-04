package pw.binom.io.httpClient

import pw.binom.URL
import pw.binom.io.*
import pw.binom.io.http.Headers
import pw.binom.writeByte
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime
import kotlin.time.measureTimedValue

internal class UrlConnectImpl(val method: String, val url: URL, val client: AsyncHttpClient) : AsyncHttpClient.UrlConnect {
    override val headers: MutableMap<String, MutableList<String>> = HashMap()
    private var requestSent = false
    private var clientDataLength: ULong? = null
    private var chanked = false
    private var channel: AsyncHttpClient.Connection? = null

    init {
        headers[Headers.CONNECTION] = mutableListOf(Headers.KEEP_ALIVE)
        headers[Headers.HOST] = mutableListOf(url.host)
        headers[Headers.ACCEPT_ENCODING] = mutableListOf("gzip, deflate, identity")
//        headers[Headers.ACCEPT_ENCODING] = mutableListOf("identity")
    }

    private fun checkSent() {
        if (requestSent) {
            throw IllegalStateException("Request already sent")
        }
    }

    @OptIn(ExperimentalTime::class)
    private suspend fun sendRequest(withOutput: Boolean) {
        checkSent()
        requestSent = true

        clientDataLength = headers[Headers.CONTENT_LENGTH]?.singleOrNull()?.let {
            it.toULongOrNull() ?: throw RuntimeException("Can't parse Content-Length \"$it\" to unsigned long")
        }
        if (clientDataLength == null && withOutput) {
            if (headers[Headers.TRANSFER_ENCODING]?.isEmpty() == true) {
                headers.getOrPut(Headers.TRANSFER_ENCODING) { ArrayList() }.add(Headers.CHUNKED)
                chanked = true
            }
        }
        if (!chanked) {
            chanked = clientDataLength == null && headers[Headers.TRANSFER_ENCODING]?.singleOrNull() == Headers.CHUNKED
        }
        if (!withOutput && clientDataLength.let { it != null && it > 0uL }) {
            throw IllegalStateException("Request without body must be inited without Content-Length")
        }
        val connection = client.borrowConnection(url)
        val buffered = connection.channel.bufferedOutput(closeStream = false)
        val app = buffered.utf8Appendable()
        app.append("$method ${url.uri} HTTP/1.1\r\n")
        headers.forEach { en ->
            en.value.forEach {
                app.append(en.key)
                app.append(": ")
                app.append(it)
                app.append("\r\n")
            }
        }
        app.append("\r\n")
        val sendTime = measureTime {
            buffered.flush()
        }
        println("Send Request Header: $sendTime")
        buffered.close()
        channel = connection
    }

    override suspend fun upload(): AsyncHttpClient.UrlRequest {
        sendRequest(true)

//        val chanked = clientDataLength == null && headers[Headers.TRANSFER_ENCODING]?.singleOrNull() == Headers.CHUNKED
//        if (!chanked && clientDataLength == null) {
//            throw IllegalStateException("Unknown Transfer Encoding")
//        }

        TODO()
//        return when {
//            else -> NoInputUrlRequest(connection)
//        }
    }

    @OptIn(ExperimentalTime::class)
    internal suspend fun compliteRequest(): UrlResponseImpl {
        val channel = channel!!
        val buffered = channel.channel.bufferedInput(closeStream = false)
        val reader = buffered.utf8Reader()
        val vv = measureTimedValue {
            reader.readln()
        }
        println("Read Response Time: ${vv.duration}")
        val responseLine = vv.value
        if (responseLine == null) {
            channel.sslSession?.close()
            channel.channel.close()
            throw IOException("Invalid reponse line: Reponse is empty")
        }
        if (!responseLine.startsWith("HTTP/1.1 "))
            throw IOException("Unsupported HTTP version. Response: \"$responseLine\"")
        val responseCode = responseLine.substring(9, 12).toInt()
        val responseHeaders = HashMap<String, ArrayList<String>>()
        while (true) {
            val str = reader.readln() ?: ""
            if (str.isEmpty()) {
                break
            }
            val items = str.split(": ")
            responseHeaders.getOrPut(items[0]) { ArrayList() }.add(items[1])
        }
        this.channel = null
        return UrlResponseImpl(
                responseCode = responseCode,
                headers = responseHeaders,
                channel = channel,
                client = client,
                url = url,
                input = buffered
        )
    }

    @OptIn(ExperimentalTime::class)
    override suspend fun response(): AsyncHttpClient.UrlResponse {
        sendRequest(false)
        val channel = channel!!
        if (chanked) {
            channel.channel.writeByte('0'.toByte())
            channel.channel.writeByte('\r'.toByte())
            channel.channel.writeByte('\n'.toByte())
            channel.channel.writeByte('\r'.toByte())
            channel.channel.writeByte('\n'.toByte())
        }
        return compliteRequest()
    }

    override suspend fun close() {
        channel?.let {
            it.sslSession?.close()
            it.channel.close()
        }
    }

}