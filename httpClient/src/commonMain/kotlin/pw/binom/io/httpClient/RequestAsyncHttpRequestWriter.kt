package pw.binom.io.httpClient

import pw.binom.DEFAULT_BUFFER_SIZE
import pw.binom.charset.Charset
import pw.binom.io.AsyncAppendable
import pw.binom.io.StreamClosedException
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
    private var closed = false

    private fun checkClosed() {
        if (closed) {
            throw StreamClosedException()
        }
    }

    override suspend fun getResponse(): HttpResponse {
        checkClosed()
        writer.flush()
        writer.asyncClose()
        closed = true
        return output.getResponse()
    }

    override suspend fun append(value: Char): AsyncAppendable {
        checkClosed()
        writer.append(value)
        return this
    }

    override suspend fun append(value: CharSequence?): AsyncAppendable {
        checkClosed()
        writer.append(value)
        return this
    }

    override suspend fun append(value: CharSequence?, startIndex: Int, endIndex: Int): AsyncAppendable {
        checkClosed()
        writer.append(value, startIndex, endIndex)
        return this
    }

    override suspend fun flush() {
        checkClosed()
        writer.flush()
        output.flush()
    }

    override suspend fun asyncClose() {
        checkClosed()
        closed = true
        writer.flush()
        output.flush()
        writer.asyncClose()
        output.asyncClose()
    }
}
