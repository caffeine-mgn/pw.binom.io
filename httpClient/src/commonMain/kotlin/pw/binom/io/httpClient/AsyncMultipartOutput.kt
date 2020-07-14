package pw.binom.io.httpClient

import pw.binom.AsyncOutput
import pw.binom.ByteBuffer
import pw.binom.io.utf8Appendable
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

class AsyncMultipartOutput(val stream: AsyncOutput, val boundary: String = generateBoundary(), private val close: Boolean = false) : AsyncOutput {
    companion object {
        fun generateBoundary(minusCount: Int = 24, charCount: Int = 16): String {
            if (minusCount == 0 && charCount == 0)
                throw IllegalArgumentException("Arguments minusCount and/or charCount must be grate than 0")
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
    private val writer = stream.utf8Appendable()
    private suspend fun printBoundary() {
        writer.append("--").append(boundary)
    }

    suspend fun part(formName: String, headers: Map<String, List<String>> = emptyMap()) {
        if ("\r\n" in formName) {
            throw IllegalArgumentException("formName can't concate \\r\\n")
        }

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
        printBoundary()
        writer.append("\r\n")

        writer.append("Content-Disposition: form-data; name=\"").append(formName).append("\"\r\n")
        headers.forEach {
            it.value.forEach { value ->
                writer.append(it.key).append(": ").append(value).append("\r\n")
            }
        }
        writer.append("\r\n")
        first = false
    }

    override suspend fun write(data: ByteBuffer): Int {
        if (first)
            throw IllegalStateException("No defined part")
        return stream.write(data)
    }

    override suspend fun flush() {
        stream.flush()
    }

    override suspend fun close() {
        if (!first) {
            writer.append("\r\n")
            printBoundary()
            writer.append("--\r\n")
        }
        if (close) {
            stream.close()
        }
    }
}