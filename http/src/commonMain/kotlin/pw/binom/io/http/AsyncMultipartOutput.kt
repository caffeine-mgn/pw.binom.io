package pw.binom.io.http

import pw.binom.io.AsyncOutput
import pw.binom.io.ByteBuffer
import pw.binom.io.DataTransferSize
import pw.binom.io.bufferedAsciiWriter
import kotlin.random.Random

/**
 * Multipart Output.
 * Example:
 * ```
 *     val mulipart = AsyncMultipartOutput(stream)
 *     mulipart.part("login")
 *     mulipart.utf8Appendable().append("someUserLogin")
 *     mulipart.close()
 * ```
 */
private val BOUNDARY_CHARS = arrayOf(
    '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
    'a', 'b', 'c', 'd', 'e', 'f'
)

class AsyncMultipartOutput(
    val stream: AsyncOutput,
    val boundary: String = generateBoundary(),
    private val closeParent: Boolean = true
) : AsyncOutput {
    companion object {
        fun generateBoundary(minusCount: Int = 24, charCount: Int = 16): String {
            if (minusCount == 0 && charCount == 0)
                throw IllegalArgumentException("Arguments minusCount and charCount must be grate than 0")
            val sb = StringBuilder()
            repeat(minusCount) {
                sb.append('-')
            }
            repeat(charCount) {
                sb.append(BOUNDARY_CHARS[Random.nextInt(0, BOUNDARY_CHARS.size)])
            }
            return sb.toString()
        }
    }

    private var first = true
    private val writer = stream.bufferedAsciiWriter()
    private suspend fun printBoundary() {
        writer.append("--").append(boundary)
    }

    suspend fun formData(formName: String, headers: Headers = emptyHeaders()) {
        if (Utils.CRLF in formName) {
            throw IllegalArgumentException("formName can't concate \\r\\n")
        }
        internalPart(headers = headers)

        writer.append("Content-Disposition: form-data; name=\"").append(formName).append("\"").append(Utils.CRLF)
        writer.append(Utils.CRLF)
    }

    suspend fun part(mimeType: String, headers: Headers = emptyHeaders()) {
        if (Headers.CONTENT_TYPE in headers) {
            throw IllegalArgumentException("Headers already contains header ${Headers.CONTENT_TYPE}")
        }
        internalPart(headers = headers)
        writer.append("Content-Type: ").append(mimeType).append(Utils.CRLF)
        writer.append(Utils.CRLF)
    }

    suspend fun part(headers: Headers = emptyHeaders()) {
        internalPart(headers = headers)
        writer.append(Utils.CRLF)
    }

    private suspend fun internalPart(headers: Headers = emptyHeaders()) {
        headers.forEach {
            if (Utils.CRLF in it.value) {
                throw IllegalArgumentException("Header Name can't concate \\r\\n")
            }
            it.value.forEach { value ->
                if (Utils.CRLF in value) {
                    throw IllegalArgumentException("Header Value can't concate \\r\\n")
                }
            }
        }

        if (!first) {
            writer.append(Utils.CRLF)
        }
        first = false
        printBoundary()
        writer.append(Utils.CRLF)
        headers.forEach {
            it.value.forEach { value ->
                writer.append(it.key).append(": ").append(value).append(Utils.CRLF)
            }
        }
    }

    override suspend fun write(data: ByteBuffer): DataTransferSize {
        writer.flush()
        if (first) {
            throw IllegalStateException("No defined part")
        }
        return stream.write(data)
    }

    override suspend fun flush() {
        writer.flush()
    }

    override suspend fun asyncClose() {
        if (!first) {
            writer.append(Utils.CRLF)
            printBoundary()
            writer.append("--").append(Utils.CRLF)
            writer.flush()
        }
        if (closeParent) {
            stream.asyncClose()
        }
    }
}
