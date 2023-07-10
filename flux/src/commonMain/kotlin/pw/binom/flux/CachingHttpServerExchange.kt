package pw.binom.flux

import pw.binom.DEFAULT_BUFFER_SIZE
import pw.binom.asyncInput
import pw.binom.asyncOutput
import pw.binom.copyTo
import pw.binom.io.*
import pw.binom.io.http.HashHeaders2
import pw.binom.io.http.Headers
import pw.binom.io.http.MutableHeaders
import pw.binom.io.httpServer.HttpServerExchange
import pw.binom.url.Path
import pw.binom.url.URI

class CachingHttpServerExchange(val source: HttpServerExchange) : HttpServerExchange {
    override val requestURI: URI
        get() = source.requestURI
    override val input: AsyncInput
        get() {
            val inputStream = inputStream
            if (inputStream != null) {
                return inputStream
            }
            val i = source.input
            inputAlreadyReading = true
            return i
        }
    override val output: AsyncOutput
        get() {
            check(headersSent) { "Output Stream access only after header send" }
            return outputStream ?: throw IllegalStateException("Output stream not ready")
        }
    override val requestHeaders: Headers
        get() = source.requestHeaders
    override val requestMethod: String
        get() = source.requestMethod
    override val responseStarted: Boolean
        get() = headersSent
  override val requestContext: Path
    get() = source.requestContext

  var bufferedOutput: ByteArrayOutput? = null
        private set
    private var outputStream: AsyncOutput? = null

    private var headersSent = false

    var responseStatus = 0
    var responseHeaders: MutableHeaders = HashHeaders2()
    var inputData: ByteArray? = null
    var inputStream: AsyncInput? = null
    private var inputCached = false
    private var inputAlreadyReading = false
    suspend fun readInput(bufferSize: Int = DEFAULT_BUFFER_SIZE) {
        ByteBuffer(bufferSize).use { buffer ->
            readInput(buffer)
        }
    }

    suspend fun readInput(buffer: ByteBuffer) {
        check(!inputAlreadyReading) { "Input already reading" }
        check(!inputCached) { "Input already cached" }
        ByteArrayOutput().use { buf ->
            source.input.copyTo(buf, buffer = buffer)
            val inputData = buf.locked {
                it.toByteArray()
            }
            this.inputData = inputData
            inputStream = ByteArrayInput(inputData).asyncInput()
            inputCached = true
        }
    }

    override suspend fun startResponse(statusCode: Int, headers: Headers) {
        check(!headersSent) { "Headers already sent" }
        responseStatus = statusCode
        headersSent = true
        responseHeaders.clear()
        responseHeaders.add(headers)
        val bufferedOutput = ByteArrayOutput()
        this.bufferedOutput = bufferedOutput
        outputStream = bufferedOutput.asyncOutput(callClose = false)
    }

    private var flushed = false

    suspend fun flush() {
        if (flushed) {
            return
        }
        flushed = true
        source.startResponse(responseStatus, responseHeaders)
        bufferedOutput?.use { buffer ->
            buffer.locked { data ->
                source.output.writeFully(data)
            }
        }
    }
}
