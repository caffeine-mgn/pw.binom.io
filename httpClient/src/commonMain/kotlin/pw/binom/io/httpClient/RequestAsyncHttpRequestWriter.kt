package pw.binom.io.httpClient

import pw.binom.DEFAULT_BUFFER_SIZE
import pw.binom.charset.Charset
import pw.binom.io.AsyncAppendable
import pw.binom.io.bufferedWriter

class RequestAsyncHttpRequestWriter(
    val output: AsyncHttpRequestOutput,
    bufferSize: Int = DEFAULT_BUFFER_SIZE,
    charBufferSize: Int = bufferSize / 2,
    charset: Charset,
) : AsyncHttpRequestWriter {
    private val writer = output.bufferedWriter(
        bufferSize = bufferSize,
        charset = charset,
        charBufferSize = charBufferSize,
        closeParent = false
    )

    override suspend fun getResponse(): HttpResponse {
        writer.flush()
        writer.asyncClose()
        return output.getResponse()
    }

    override suspend fun append(c: Char): AsyncAppendable {
        writer.append(c)
        return this
    }

    override suspend fun append(csq: CharSequence?): AsyncAppendable {
        writer.append(csq)
        return this
    }

    override suspend fun append(csq: CharSequence?, start: Int, end: Int): AsyncAppendable {
        writer.append(csq, start, end)
        return this
    }

    override suspend fun flush() {
        writer.flush()
        output.flush()
    }

    override suspend fun asyncClose() {
        writer.flush()
        output.flush()
        writer.asyncClose()
        output.asyncClose()
    }
}