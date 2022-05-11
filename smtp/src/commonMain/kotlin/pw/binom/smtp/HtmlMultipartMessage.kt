package pw.binom.smtp

import pw.binom.ByteBufferPool
import pw.binom.charset.Charset
import pw.binom.charset.Charsets
import pw.binom.io.*
import pw.binom.io.http.AsyncMultipartOutput
import pw.binom.io.http.headersOf

class HtmlMultipartMessage internal constructor(val output: AsyncOutput) : Message {

    private val multipart = AsyncMultipartOutput(output, closeParent = false)
    private val pool = ByteBufferPool(1)

    internal suspend fun start(from: String, fromAlias: String?, to: String, toAlias: String?, subject: String?) {
        val output = StringBuilder()
        output.append("From: ")
        if (fromAlias != null) {
            output.append(fromAlias).append(" ")
        }
        output.append("<").append(from).append(">\r\n")
            .append("To: ")
        if (toAlias != null) {
            output.append(toAlias).append(" ")
        }
        output.append("<").append(to).append(">\r\n")
            .append("Mime-Version: 1.0\r\n")
        if (subject != null) {
            output.append("Subject: ").append(subject).append("\r\n")
        }
        output.append("Content-Type: multipart/mixed;boundary=\"").append(multipart.boundary).append("\"\r\n\r\n")
        this.output.bufferedWriter(closeParent = false, charset = Charsets.UTF8).use {
            it.append(output.toString())
            it.flush()
        }
        this.output
    }

    suspend fun appendText(mimeType: String, charset: Charset = Charsets.UTF8): AsyncWriter {
        multipart.part(
            mimeType = mimeType + ";charset=${charset.name}"
        )
        multipart.flush()
        return multipart.bufferedWriter(pool = pool, charset = charset, closeParent = false)
    }

    suspend fun attach(mimeType: String = "application/octet-stream", name: String): AsyncOutput {
        multipart.part(
            mimeType = "$mimeType;name=$name",
            headers = headersOf(
                "Content-Transfer-Encoding" to "Binary",
                "Content-Disposition" to "attachment;filename=\"${UTF8.encode(name)}\"",
            )
        )
        multipart.flush()
        return FlashOnCloseAsyncOutput(multipart)
    }

    internal suspend fun finish() {
        multipart.flush()
        multipart.asyncClose()
    }
}

class FlashOnCloseAsyncOutput(val output: AsyncOutput) : AsyncOutput {
    override suspend fun write(data: ByteBuffer): Int = output.write(data)

    override suspend fun asyncClose() {
        output.flush()
    }

    override suspend fun flush() {
        output.flush()
    }
}
