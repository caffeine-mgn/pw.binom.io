package pw.binom.compression.zlib

import pw.binom.io.Closeable
import java.util.zip.Deflater as JDeflater

actual class Deflater actual constructor(level: Int, wrap: Boolean) : Closeable {
    private val native = JDeflater(level, !wrap)
    override fun close() {
        native.end()
    }

    actual constructor() : this(6, true)

    actual fun deflate(cursor: Cursor, input: ByteArray, output: ByteArray) {
        native.setInput(input, cursor.inputOffset, cursor.inputLength)
        val readed = native.bytesRead
        val writed = native.bytesWritten
        native.deflate(output, cursor.outputOffset, cursor.outputLength, JDeflater.NO_FLUSH)
        cursor.inputOffset += (native.bytesRead - readed).toInt()
        cursor.outputOffset += (native.bytesWritten - writed).toInt()
    }

    actual fun flush(cursor: Cursor, output: ByteArray) {
        val readed = native.bytesRead
        val writed = native.bytesWritten
        native.deflate(output, cursor.outputOffset, cursor.outputLength, JDeflater.SYNC_FLUSH)
        cursor.inputOffset += (native.bytesRead - readed).toInt()
        cursor.outputOffset += (native.bytesWritten - writed).toInt()
    }

}