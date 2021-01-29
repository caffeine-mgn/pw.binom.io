package pw.binom.io.httpServer

import pw.binom.AsyncInput
import pw.binom.AsyncOutput
import pw.binom.ByteBuffer
import pw.binom.EmptyAsyncInput
import pw.binom.io.AsyncBufferedAsciiInputReader
import pw.binom.io.IOException
import pw.binom.io.http.AsyncChunkedInput
import pw.binom.io.http.AsyncContentLengthInput
import pw.binom.io.http.Headers
import pw.binom.io.readln
import pw.binom.io.utf8Reader
import pw.binom.network.CrossThreadKeyHolder
import pw.binom.network.TcpConnection

internal enum class EncodeType {
    GZIP,
    DEFLATE,
    IDENTITY
}

internal class HttpRequestImpl2 : HttpRequest {
    override lateinit var method: String
        private set

    override lateinit var uri: String
        private set

    override val contextUri: String
        get() = uri

    private var wrapped: AsyncInput? = null

    override val input: AsyncInput
        get() = wrapped!!

    var _rawInput: AsyncBufferedAsciiInputReader? = null
    override val rawInput: AsyncBufferedAsciiInputReader
        get() = _rawInput!!

    var _rawOutput: PoolAsyncBufferedOutput? = null
    override val rawOutput: AsyncOutput
        get() = _rawOutput!!

    private var _rawConnection: TcpConnection? = null

    override val rawConnection: TcpConnection
        get() = _rawConnection!!

    override val headers = HashMap<String, ArrayList<String>>()
    override val keyHolder: CrossThreadKeyHolder
        get() = rawConnection.holder

    var encode = EncodeType.IDENTITY
        private set

    var keepAlive = false
        private set

    fun flush() {
        wrapped = null
    }

    suspend fun init(
        method: String,
        uri: String,
        input: AsyncBufferedAsciiInputReader,
        output: PoolAsyncBufferedOutput,
        rawConnection: TcpConnection,
        allowZlib: Boolean
    ) {
        _rawInput = input
        _rawOutput = output
        _rawConnection = rawConnection
        this.method = method
        this.uri = uri
        headers.clear()
        while (true) {
            val s = input.readln() ?: break
            if (s.isEmpty()) {
                break
            }
            val p = s.indexOf(':')
            if (p < 0) {
                throw IOException("Invalid HTTP Header")
            }
            val headerKey = s.substring(0, p)
            val headerValue = s.substring(p + 2)
            headers.getOrPut(headerKey) { ArrayList() }.add(headerValue)
        }
        encode = headers[Headers.ACCEPT_ENCODING]?.flatMap {
            it.split(',')
        }?.map { it.trim().toLowerCase() }?.map {
            when {
                allowZlib && it == "gzip" -> EncodeType.GZIP
                allowZlib && it == "deflate" -> EncodeType.DEFLATE
                else -> EncodeType.IDENTITY
            }
        }
            ?.firstOrNull() ?: EncodeType.IDENTITY
        keepAlive = headers[Headers.CONNECTION]?.any { it.toLowerCase() == Headers.KEEP_ALIVE } ?: false
        wrapped = input
        wrapped =
            if (headers[Headers.TRANSFER_ENCODING]?.asSequence()?.flatMap { it.splitToSequence(',') }?.map { it.trim() }
                    ?.any { it == Headers.CHUNKED } == true) {
                AsyncChunkedInput(wrapped!!, false)
            } else {
                val contentLength = headers[Headers.CONTENT_LENGTH]?.singleOrNull()?.toULong()
                if (contentLength != null && contentLength > 0u)
                    AsyncContentLengthInput(
                        stream = wrapped!!,
                        autoCloseStream = false,
                        contentLength = contentLength
                    )
                else
                    EmptyAsyncInput
            }
    }
}