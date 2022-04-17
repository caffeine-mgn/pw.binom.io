package pw.binom.flux

import pw.binom.AsyncOutput
import pw.binom.asyncOutput
import pw.binom.charset.Charsets
import pw.binom.io.AsyncWriter
import pw.binom.io.ByteArrayOutput
import pw.binom.io.bufferedWriter
import pw.binom.io.http.MutableHeaders
import pw.binom.io.httpServer.HttpResponse

class CachingHttpResponse(val onClose: ((CachingHttpResponse) -> Unit)?) : HttpResponse {
    var original: HttpResponse? = null
        private set
    private var request: CachingHttpRequest? = null
    internal fun resetOriginal(original: HttpResponse, request: CachingHttpRequest) {
        this.original = original
        this.request = request
    }

    override val headers: MutableHeaders
        get() = original!!.headers

    override var status: Int
        get() = original!!.status
        set(value) {
            original!!.status = value
        }

    override suspend fun asyncClose() {
    }

    private val data = NoCloseByteArrayOutput()
    private val asyncData = data.asyncOutput()

    override suspend fun startWriteBinary(): AsyncOutput {
        if (original == null) {
            throw IllegalStateException("Original HttpResponse not set")
        }
        return asyncData
    }

    override suspend fun startWriteText(): AsyncWriter {
        val charset = Charsets.get(headers.charset ?: "utf-8")
        return startWriteBinary().bufferedWriter(charset = charset)
    }

    private class NoCloseByteArrayOutput : ByteArrayOutput() {
        fun forceClose() {
            super.close()
        }

        override fun close() {
        }
    }

    internal suspend fun finish() {
        if (original == null) {
            return
        }
        try {
            data.locked {
                original!!.sendBinary(it)
            }
            original!!.asyncClose()
        } finally {
            data.clear()
        }
        original = null
        onClose?.invoke(this)
    }
}
