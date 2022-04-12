package pw.binom.flux

import pw.binom.AsyncOutput
import pw.binom.asyncOutput
import pw.binom.charset.Charsets
import pw.binom.io.AsyncWriter
import pw.binom.io.ByteArrayOutput
import pw.binom.io.bufferedWriter
import pw.binom.io.http.MutableHeaders
import pw.binom.io.httpServer.HttpResponse

class CachingHttpHttpResponse(val onClose: ((CachingHttpHttpResponse) -> Unit)?) : HttpResponse {
    private var original: HttpResponse? = null
    internal fun resetOriginal(original: HttpResponse?) {
        this.original = original
    }

    override val headers: MutableHeaders
        get() = original!!.headers

    override var status: Int
        get() = original!!.status
        set(value) {
            original!!.status = value
        }

    override suspend fun asyncClose() {
        sendAndClose()
    }

    suspend fun sendAndClose() {
        try {
            data.locked {
                original!!.sendBinary(it)
            }
        } finally {
            data.clear()
        }
        onClose?.invoke(this)
    }

    internal fun free() {
        data.forceClose()
    }

    private val data = NoCloseByteArrayOutput()
    private val asyncData = data.asyncOutput()

    override suspend fun startWriteBinary(): AsyncOutput =
        asyncData

    override suspend fun startWriteText(): AsyncWriter {
        val charset = Charsets.get(headers.charset ?: "utf-8")
        return startWriteBinary().bufferedWriter(charset = charset)
    }

    class NoCloseByteArrayOutput : ByteArrayOutput() {
        fun forceClose() {
            super.close()
        }

        override fun close() {
        }
    }
}
