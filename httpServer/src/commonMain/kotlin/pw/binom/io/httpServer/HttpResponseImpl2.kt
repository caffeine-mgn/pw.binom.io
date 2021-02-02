package pw.binom.io.httpServer

import pw.binom.AsyncOutput
import pw.binom.ByteBuffer
import pw.binom.compression.zlib.AsyncDeflaterOutput
import pw.binom.compression.zlib.AsyncGZIPOutput
import pw.binom.io.http.AsyncChunkedOutput
import pw.binom.io.http.AsyncContentLengthOutput
import pw.binom.io.http.Headers
import pw.binom.pool.ObjectPool

internal class HttpResponseBodyImpl2 : HttpResponseBody {

    private var rawOutput: AsyncOutput? = null
    private var wrappedOutput: AsyncOutput? = null

    fun init(contentLength: ULong?,
             encode: EncodeType,
             rawOutput: PoolAsyncBufferedOutput,
             zlibBufferSize: Int,
             autoFlushSize: Int
    ) {
        this.rawOutput = rawOutput
        var stream:AsyncOutput = rawOutput

        stream = when {
            contentLength != null -> {
                AsyncContentLengthOutput(
                        stream = stream,
                        contentLength = contentLength,
                        closeStream = false
                )
            }
            else -> {
                AsyncChunkedOutput(
                        stream = stream,
                        autoFlushBuffer = autoFlushSize,
                        closeStream = false
                )
            }
        }

        stream =
                when (encode) {
                    EncodeType.GZIP -> AsyncGZIPOutput(stream = stream, level = 6, bufferSize = zlibBufferSize, closeStream = stream != rawOutput)
                    EncodeType.DEFLATE -> AsyncDeflaterOutput(stream, 6, wrap = true, bufferSize = zlibBufferSize, closeStream = stream != rawOutput)
                    EncodeType.IDENTITY -> stream
                }


        wrappedOutput = stream
    }

    override suspend fun write(data: ByteBuffer): Int =
            wrappedOutput!!.write(data)

    override suspend fun flush() {
        wrappedOutput!!.flush()
    }

    override suspend fun asyncClose() {
        wrappedOutput!!.asyncClose()
    }

}

internal class HttpResponseImpl2(
        val responseBodyPool: ObjectPool<HttpResponseBodyImpl2>,
        private val zlibBufferSize: Int
) : HttpResponse {
    override var status: Int = 404
        set(value) {
            checkHeaderSent()
            field = value
        }
    private var rawOutput: PoolAsyncBufferedOutput? = null
    override val headers = HashMap<String, ArrayList<String>>()
    private val headerSent: Boolean
        get() = body != null

    var keepAlive = false
        private set

    var encode = EncodeType.IDENTITY
        set(value) {
            checkHeaderSent()
            field = value
        }

    override var enableCompress = true

    fun init(
        encode: EncodeType,
        keepAlive: Boolean,
        output: PoolAsyncBufferedOutput
    ) {
        headers.clear()
        body = null
        this.encode = encode
        status = 404
        this.keepAlive = keepAlive
        enableCompress = true

        val encodeHeader = when (encode) {
            EncodeType.GZIP -> "gzip"
            EncodeType.DEFLATE -> "deflate"
            EncodeType.IDENTITY -> "identity"
        }
        resetHeader(Headers.CONTENT_ENCODING, encodeHeader)
        rawOutput = output
    }

    override fun clearHeaders() {
        checkHeaderSent()
        headers.clear()
    }

    override fun resetHeader(name: String, value: String) {
        checkHeaderSent()
        headers[name] = arrayListOf(value)
    }

    override fun addHeader(name: String, value: String) {
        checkHeaderSent()
        headers.getOrPut(name) { ArrayList() }.add(value)
    }

    override var enableKeepAlive: Boolean
        get() = keepAlive
        set(value) {
            checkHeaderSent()
            keepAlive = value
        }

    var body: HttpResponseBodyImpl2? = null

    private inline fun checkHeaderSent() {
        if (headerSent) {
            throw IllegalStateException("HttpResponse already sent")
        }
    }

    override suspend fun complete(autoFlushSize: UInt): HttpResponseBody {
        checkHeaderSent()
        val buf = rawOutput!!
//        val app = buf.utf8Appendable()
        buf.append("HTTP/1.1 $status ${statusToText(status)}\r\n")
        if (keepAlive) {
            headers.remove(Headers.CONNECTION)
            buf.append("${Headers.CONNECTION}: ${Headers.KEEP_ALIVE}\r\n")
        }
        val contentLength = headers[Headers.CONTENT_LENGTH]?.singleOrNull()?.toULongOrNull()
        val chunked = headers[Headers.TRANSFER_ENCODING]?.singleOrNull().let {
            it == null || it == Headers.CHUNKED
        }
        if (contentLength == 0uL) {
            headers.remove(Headers.CONTENT_ENCODING)
        }
        when {
            contentLength != null -> {
                headers.remove(Headers.CONTENT_LENGTH)
                headers.remove(Headers.TRANSFER_ENCODING)
                buf.append(Headers.CONTENT_LENGTH).append(": ").append(contentLength.toString()).append("\r\n")
            }
            chunked -> {
                headers.remove(Headers.CONTENT_LENGTH)
                headers.remove(Headers.TRANSFER_ENCODING)
                buf.append(Headers.TRANSFER_ENCODING).append(": ").append(Headers.CHUNKED).append("\r\n")
            }
            else -> throw RuntimeException("Unknown Transfer Encoding")
        }

        headers.forEach { item ->
            item.value.forEach {
                buf.append(item.key).append(": ").append(it).append("\r\n")
            }
        }
        buf.append("\r\n")
        body = responseBodyPool.borrow {
            it.init(
                    contentLength = contentLength,
                    encode = encode,
                    rawOutput = rawOutput!!,
                    zlibBufferSize = if (enableCompress) zlibBufferSize else 0,
                    autoFlushSize = autoFlushSize.toInt()
            )
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