package pw.binom.compression.zlib

import pw.binom.io.ByteBuffer
import pw.binom.io.Closeable
import java.util.zip.Inflater as JInflater

actual class Inflater actual constructor(wrap: Boolean) : Closeable {
    private val native = JInflater(!wrap)

    override fun close() {
        native.end()
    }

    actual fun end() {
        native.end()
    }

    actual fun inflate(input: ByteBuffer, output: ByteBuffer): Int {
        val inputData = input.toByteArray()
        val beforeReaded = native.bytesRead
        native.setInput(inputData)
        val writed = native.bytesWritten
        val outputData = ByteArray(output.remaining)
        native.inflate(outputData)
        val w = (native.bytesWritten - writed).toInt()
        output.write(outputData, 0, w)
        input.position += (native.bytesRead - beforeReaded).toInt()
        return w
    }
}
