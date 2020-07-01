package pw.binom.io.httpServer

import pw.binom.AsyncOutput
import pw.binom.ByteBuffer
import pw.binom.ByteDataBuffer
import pw.binom.compression.zlib.AsyncDeflaterOutput
import pw.binom.compression.zlib.AsyncGZIPOutput
import pw.binom.io.LazyAsyncOutput
import pw.binom.io.bufferedOutput
import pw.binom.io.http.AsyncChunkedOutput
import pw.binom.io.http.AsyncContentLengthOutput
import pw.binom.io.http.Headers
import pw.binom.io.utf8Appendable
import pw.binom.pool.DefaultPool
import pw.binom.pool.ObjectPool

internal class HttpResponseBodyImpl2(

) : HttpResponseBody {

    private var rawOutput: AsyncOutput? = null
    private var wrappedOutput: AsyncOutput? = null

    fun init(contentLength: ULong?,
             encode: EncodeType,
             rawOutput: AsyncOutput) {
        this.rawOutput = rawOutput
        val stream = when {
            contentLength != null -> {
                AsyncContentLengthOutput(rawOutput, contentLength)
            }
            else -> {
                AsyncChunkedOutput(rawOutput)
            }
        }
        wrappedOutput = when (encode) {
            EncodeType.GZIP -> AsyncGZIPOutput(stream, 6)
            EncodeType.DEFLATE -> AsyncDeflaterOutput(stream, 6, wrap = true)
            EncodeType.IDENTITY -> stream
        }
    }

//    override suspend fun write(data: ByteDataBuffer, offset: Int, length: Int): Int =
//            wrappedOutput!!.write(data, offset, length)

    override suspend fun write(data: ByteBuffer): Int =
            wrappedOutput!!.write(data)

    override suspend fun flush() {
        wrappedOutput!!.flush()
    }

    override suspend fun close() {
        wrappedOutput!!.close()
    }

}

internal class HttpResponseImpl2(val responseBodyPool: ObjectPool<HttpResponseBodyImpl2>
) : HttpResponse {
    override var status: Int = 404
    private var rawOutput: AsyncOutput? = null
    override val headers = HashMap<String, ArrayList<String>>()
    var keepAlive = false
        private set
//
//    private suspend fun completeHeader(
//            encode: EncodeType,
//            outputPool: DefaultPool<NoCloseOutput>
//    ) {
//        val buf = rawOutput!!
//        val app = buf.utf8Appendable()
//        app.append("HTTP/1.1 $status ${statusToText(status)}\r\n")
//        if (keepAlive) {
//            headers.remove(Headers.CONNECTION)
//            app.append("${Headers.CONNECTION}: ${Headers.KEEP_ALIVE}\r\n")
//        }
//        val contentLength = headers[Headers.CONTENT_LENGTH]?.singleOrNull()?.toULongOrNull()
//        val chanked = headers[Headers.TRANSFER_ENCODING]?.singleOrNull().let {
//            it == null || it == Headers.CHUNKED
//        }
//
//        if (headers[Headers.SERVER] == null) {
//            app.append("${Headers.SERVER}: Binom Server\r\n")
//        }
//
//        val noClose = outputPool.borrow {
//            it.stream = buf
//        }
//
//         when {
//            contentLength != null -> {
//                headers.remove(Headers.CONTENT_LENGTH)
//                headers.remove(Headers.TRANSFER_ENCODING)
//                app.append("${Headers.CONTENT_LENGTH}: $contentLength\r\n")
//            }
//            chanked -> {
//                headers.remove(Headers.CONTENT_LENGTH)
//                headers.remove(Headers.TRANSFER_ENCODING)
//                app.append("${Headers.TRANSFER_ENCODING}: ${Headers.CHUNKED}\r\n")
//            }
//            else -> throw RuntimeException("Unknown Transfer Encoding")
//        }
//        headers.forEach { item ->
//            item.value.forEach {
//                app.append("${item.key}: $it\r\n")
//            }
//        }
//        app.append("\r\n")
//    }

    var encode = EncodeType.IDENTITY

    fun init(encode: EncodeType,
             keepAlive: Boolean,
             output: AsyncOutput) {
        headers.clear()
        body = null
        this.encode = encode
        status = 404
        this.keepAlive = keepAlive

        val encodeHeader = when (encode) {
            EncodeType.GZIP -> "gzip"
            EncodeType.DEFLATE -> "deflate"
            EncodeType.IDENTITY -> "identity"
        }
        resetHeader(Headers.CONTENT_ENCODING, encodeHeader)
        rawOutput = output
    }

    override fun clearHeaders() {
        headers.clear()
    }

    override fun resetHeader(name: String, value: String) {
        headers[name] = arrayListOf(value)
    }

    override fun addHeader(name: String, value: String) {
        headers.getOrPut(name) { ArrayList() }.add(value)
    }

    override fun detach(): HttpConnectionState {
        TODO("Not yet implemented")
    }

    var body: HttpResponseBodyImpl2? = null

    override suspend fun complete(): HttpResponseBody {
        val buf = rawOutput!!//!!.bufferedOutput()
        val app = buf.utf8Appendable()
        app.append("HTTP/1.1 $status ${statusToText(status)}\r\n")
        if (keepAlive) {
            headers.remove(Headers.CONNECTION)
            app.append("${Headers.CONNECTION}: ${Headers.KEEP_ALIVE}\r\n")
        }
        val contentLength = headers[Headers.CONTENT_LENGTH]?.singleOrNull()?.toULongOrNull()
        val chanked = headers[Headers.TRANSFER_ENCODING]?.singleOrNull().let {
            it == null || it == Headers.CHUNKED
        }

        when {
            contentLength != null -> {
                headers.remove(Headers.CONTENT_LENGTH)
                headers.remove(Headers.TRANSFER_ENCODING)
                app.append("${Headers.CONTENT_LENGTH}: $contentLength\r\n")
            }
            chanked -> {
                headers.remove(Headers.CONTENT_LENGTH)
                headers.remove(Headers.TRANSFER_ENCODING)
                app.append("${Headers.TRANSFER_ENCODING}: ${Headers.CHUNKED}\r\n")
            }
            else -> throw RuntimeException("Unknown Transfer Encoding")
        }

        headers.forEach { item ->
            item.value.forEach {
                app.append("${item.key}: $it\r\n")
            }
        }
        app.append("\r\n")
        body = responseBodyPool.borrow {
            it.init(contentLength, encode, rawOutput!!)
        }
        rawOutput = null
        buf.flush()
        return body!!
    }
}

private fun statusToText(code: Int) =
        when (code) {
            100 -> "Continue"
            101 -> "Switching Protocols"
            102 -> "Processing"
            200 -> "OK"
            201 -> "Created"
            202 -> "Accepted"
            203 -> "Non-Authoritative Information"
            204 -> "No Content"
            205 -> "Reset Content"
            206 -> "Partial Content"
            207 -> "Multi-Status"
            208 -> "Already Reported"
            300 -> "Multiple Choices"
            301 -> "Moved Permanently"
            302 -> "Found"
            303 -> "See Other"
            304 -> "Not Modified"
            305 -> "Use Proxy"
            307 -> "Temporary Redirect"
            308 -> "Permanent Redirect"
            400 -> "Bad Request"
            401 -> "Unauthorized"
            402 -> "Payment Required"
            403 -> "Forbidden"
            404 -> "Not Found"
            405 -> "Method Not Allowed"
            406 -> "Not Acceptable"
            407 -> "Proxy Authentication Required"
            408 -> "Request Timeout"
            409 -> "Conflict"
            410 -> "Gone"
            411 -> "Length Required"
            412 -> "Precondition Failed"
            413 -> "Payload Too Large"
            414 -> "URI Too Long"
            415 -> "Unsupported Media Type"
            416 -> "Range Not Satisfiable"
            417 -> "Expectation Failed"
            418 -> "I'm a teapot"
            419 -> "Authentication Timeout"
            421 -> "Misdirected Request"
            422 -> "Unprocessable Entity"
            423 -> "Locked"
            424 -> "Failed Dependency"
            426 -> "Upgrade Required"
            428 -> "Precondition Required"
            429 -> "Too Many Requests"
            431 -> "Request Header Fields Too Large"
            434 -> "Requested host unavailable"
            449 -> "Retry With"
            451 -> "Unavailable For Legal Reasons"
            499 -> "Client Closed Request"
            500 -> "Internal Server Error"
            501 -> "Not Implemented"
            502 -> "Bad Gateway"
            503 -> "Service Unavailable"
            504 -> "Gateway Timeout"
            505 -> "HTTP Version Not Supported"
            506 -> "Variant Also Negotiates"
            507 -> "Insufficient Storage"
            509 -> "Bandwidth Limit Exceeded"
            510 -> "Not Extended"
            511 -> "Network Authentication Required"
            520 -> "Unknown Error"
            521 -> "Web Server Is Down"
            522 -> "Connection Timed Out"
            523 -> "Origin Is Unreachable"
            524 -> "A Timeout Occurred"
            525 -> "SSL Handshake Failed"
            526 -> "Invalid SSL Certificate"
            else -> "Unknown Status"
        }