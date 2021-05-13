package pw.binom.io.httpServer

import pw.binom.AsyncInput
import pw.binom.AsyncOutput
import pw.binom.EmptyAsyncInput
import pw.binom.io.AsyncBufferedAsciiInputReader
import pw.binom.io.IOException
import pw.binom.io.http.AsyncChunkedInput
import pw.binom.io.http.AsyncContentLengthInput
import pw.binom.io.http.Headers
import pw.binom.network.CrossThreadKeyHolder
import pw.binom.network.TcpConnection

@Deprecated(message = "Will be removed")
internal enum class EncodeTypeDeprecated {
    GZIP,
    DEFLATE,
    IDENTITY
}

@Deprecated(message = "Will be removed")
internal class HttpRequestImpl2Deprecated : HttpRequestDeprecated {
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

    var _rawOutput: PoolAsyncBufferedOutputDeprecated? = null
    override val rawOutput: AsyncOutput
        get() = _rawOutput!!

    private var _rawConnection: TcpConnection? = null

    override val rawConnection: TcpConnection
        get() = _rawConnection!!

    override val headers = HashMap<String, ArrayList<String>>()

    var encode = EncodeTypeDeprecated.IDENTITY
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
        output: PoolAsyncBufferedOutputDeprecated,
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
        }?.map { it.trim().lowercase() }?.map {
            when {
                allowZlib && it == "gzip" -> EncodeTypeDeprecated.GZIP
                allowZlib && it == "deflate" -> EncodeTypeDeprecated.DEFLATE
                else -> EncodeTypeDeprecated.IDENTITY
            }
        }
            ?.firstOrNull() ?: EncodeTypeDeprecated.IDENTITY
        keepAlive = headers[Headers.CONNECTION]?.any { it.lowercase() == Headers.KEEP_ALIVE } ?: false
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
                        closeStream = false,
                        contentLength = contentLength
                    )
                else
                    EmptyAsyncInput
            }
    }
}