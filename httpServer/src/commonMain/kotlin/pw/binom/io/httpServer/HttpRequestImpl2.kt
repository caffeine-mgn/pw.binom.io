package pw.binom.io.httpServer

import pw.binom.AsyncInput
import pw.binom.io.*
import pw.binom.io.http.Headers
import pw.binom.pool.DefaultPool

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

    private var wrapped: NoCloseInput? = null

    override val input: AsyncInput
        get() = wrapped!!

    override val headers = HashMap<String, ArrayList<String>>()

    var encode = EncodeType.IDENTITY
        private set

    var keepAlive = false
        private set

    fun flush(inputBufferPool: DefaultPool<NoCloseInput>) {
        wrapped?.let {
            inputBufferPool.recycle(it)
            it.stream = null
            wrapped = null
        }
    }

    suspend fun init(method: String, uri: String, input: AsyncInput, inputBufferPool: DefaultPool<NoCloseInput>) {
        this.method = method
        this.uri = uri
        headers.clear()
        println("read headers...")
        val reader = input.utf8Reader()
        while (true) {
            val s = reader.readln()?:break
            if (s.isEmpty())
                break
            val items1 = s.split(": ", limit = 2)

            headers.getOrPut(items1[0]) { ArrayList() }.add(items1.getOrNull(1) ?: "")
        }
        encode = headers[Headers.ACCEPT_ENCODING]?.asSequence()?.flatMap {
            it.splitToSequence(',')
        }?.map { it.trim().toLowerCase() }
                ?.mapNotNull {
                    when (it) {
                        "gzip" -> EncodeType.GZIP
                        "deflate" -> EncodeType.DEFLATE
                        "identity" -> EncodeType.IDENTITY
                        else -> null
                    }
                }
                ?.firstOrNull() ?: EncodeType.IDENTITY
        keepAlive = headers[Headers.CONNECTION]?.any { it.toLowerCase() == Headers.KEEP_ALIVE } ?: false
        wrapped = inputBufferPool.borrow {
            it.stream = input
        }
    }
}