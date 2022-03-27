package pw.binom.flux

import pw.binom.AsyncOutput
import pw.binom.asyncOutput
import pw.binom.charset.Charsets
import pw.binom.io.AsyncWriter
import pw.binom.io.ByteArrayOutput
import pw.binom.io.bufferedWriter
import pw.binom.io.http.MutableHeaders
import pw.binom.io.httpServer.HttpResponse

class CachingHttpHttpResponse(val original: HttpResponse) : HttpResponse {
    override val headers: MutableHeaders
        get() = original.headers

    override var status: Int
        get() = original.status
        set(value) {
            original.status = value
        }

    override suspend fun asyncClose() {
    }

    suspend fun sendAndClose() {
        try {
            data.trimToSize()
            data.data.clear()
            original.sendBinary(data.data)
        } finally {
            data.close()
        }
    }

    private val data = ByteArrayOutput()

    override suspend fun startWriteBinary(): AsyncOutput =
        NoCloseAsyncOutput(data.asyncOutput())

    override suspend fun startWriteText(): AsyncWriter {
        val charset = Charsets.get(headers.charset ?: "utf-8")
        return startWriteBinary().bufferedWriter(charset = charset)
    }

    private class NoCloseAsyncOutput(val original: AsyncOutput) : AsyncOutput by original {
        override suspend fun asyncClose() {
        }
    }
}
