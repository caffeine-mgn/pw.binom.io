package pw.binom.io.http

import pw.binom.AsyncOutput
import pw.binom.ByteBuffer
import pw.binom.io.bufferedAsciiInputWriter
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
    private val writer = stream.bufferedAsciiInputWriter()
    private suspend fun printBoundary() {
        writer.append("--").append(boundary)
    }

    suspend fun formData(formName: String, headers: Map<String, List<String>> = emptyMap()) {
        if ("\r\n" in formName) {
            throw IllegalArgumentException("formName can't concate \\r\\n")
        }
        internalPart(headers = headers)

        writer.append("Content-Disposition: form-data; name=\"").append(formName).append("\"\r\n")
        writer.append("\r\n")
    }

    suspend fun part(mimeType: String, headers: Map<String, List<String>> = emptyMap()) {
        internalPart(headers = headers)
        writer.append("Content-Type: ").append(mimeType).append("\r\n")
        writer.append("\r\n")
    }

    private suspend fun internalPart(headers: Map<String, List<String>> = emptyMap()) {

        headers.forEach {
            if ("\r\n" in it.value)
                throw IllegalArgumentException("Header Name can't concate \\r\\n")
            it.value.forEach { value ->
                if ("\r\n" in value)
                    throw IllegalArgumentException("Header Value can't concate \\r\\n")
            }
        }

        if (!first) {
            writer.append("\r\n")
        }
        first = false
        printBoundary()
        writer.append("\r\n")
        headers.forEach {
            it.value.forEach { value ->
                writer.append(it.key).append(": ").append(value).append("\r\n")
            }
        }
    }

    override suspend fun write(data: ByteBuffer): Int {
        writer.flush()
        if (first)
            throw IllegalStateException("No defined part")
        return stream.write(data)
    }

    override suspend fun flush() {
        writer.flush()
    }

    override suspend fun asyncClose() {
        if (!first) {
            writer.append("\r\n")
            printBoundary()
            writer.append("--\r\n")
            writer.flush()
        }
        if (closeParent) {
            stream.asyncClose()
        }
    }
}