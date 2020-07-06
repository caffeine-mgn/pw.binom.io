package pw.binom.io.httpServer

import pw.binom.AsyncInput
import pw.binom.ByteBuffer
import pw.binom.io.http.AsyncChunkedInput
import pw.binom.io.http.AsyncContentLengthInput
import pw.binom.io.http.Headers
import pw.binom.io.readln
import pw.binom.io.utf8Reader

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

    override val headers = HashMap<String, ArrayList<String>>()

    var encode = EncodeType.IDENTITY
        private set

    var keepAlive = false
        private set

    fun flush() {
        wrapped = null
    }

    suspend fun init(method: String, uri: String, input: AsyncInput, allowZlib: Boolean/*, inputBufferPool: DefaultPool<NoCloseInput>*/) {
        this.method = method
        this.uri = uri
        headers.clear()
        val reader = input.utf8Reader()
        while (true) {
            val s = reader.readln() ?: break
            if (s.isEmpty())
                break
            val items1 = s.split(": ", limit = 2)

            headers.getOrPut(items1[0]) { ArrayList() }.add(items1.getOrNull(1) ?: "")
        }
        encode = headers[Headers.ACCEPT_ENCODING]?.asSequence()?.flatMap {
            it.splitToSequence(',')
        }?.map { it.trim().toLowerCase() }
                ?.mapNotNull {
                    when {
                        allowZlib && it == "gzip" -> EncodeType.GZIP
                        allowZlib && it == "deflate" -> EncodeType.DEFLATE
                        else -> EncodeType.IDENTITY
                    }
                }
                ?.firstOrNull() ?: EncodeType.IDENTITY
        keepAlive = headers[Headers.CONNECTION]?.any { it.toLowerCase() == Headers.KEEP_ALIVE } ?: false
        wrapped = input
        if (headers[Headers.TRANSFER_ENCODING]?.asSequence()?.flatMap { it.splitToSequence(',') }?.map { it.trim() }?.any { it == Headers.CHUNKED } == true) {
            wrapped = AsyncChunkedInput(wrapped!!, false)
        } else {
            val contentLength = headers[Headers.CONTENT_LENGTH]?.singleOrNull()?.toULongOrNull()
            if (contentLength != null)
                wrapped = AsyncContentLengthInput(
                        stream = wrapped!!,
                        autoCloseStream = false,
                        contentLength = contentLength
                )
            else
                wrapped = AsyncEofInput
        }
    }
}

private object AsyncEofInput : AsyncInput {
    override suspend fun read(dest: ByteBuffer): Int = 0

    override suspend fun close() {
    }

}