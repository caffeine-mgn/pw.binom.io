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
        native.setInput(input.native)
        val writed = native.bytesWritten
        native.inflate(output.native)
        return (native.bytesWritten - writed).toInt()
    }
}
