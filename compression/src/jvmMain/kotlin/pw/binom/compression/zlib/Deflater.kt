package pw.binom.compression.zlib

import pw.binom.io.Closeable
import java.util.zip.Deflater as JDeflater

actual class Deflater actual constructor(level: Int, wrap: Boolean, val syncFlush: Boolean) : Closeable {
    private val native = JDeflater(level, !wrap)
    override fun close() {

    }

    actual constructor() : this(6, true, true)

    actual fun deflate(cursor: Cursor, input: ByteArray, output: ByteArray): Int {
        native.setInput(input, cursor.inputOffset, cursor.inputLength)
        val readed = native.bytesRead
        val writed = native.bytesWritten
        native.deflate(output, cursor.outputOffset, cursor.outputLength, JDeflater.NO_FLUSH)
        cursor.inputOffset += (native.bytesRead - readed).toInt()
        cursor.outputOffset += (native.bytesWritten - writed).toInt()

        _totalIn += native.bytesRead - readed
        _totalOut += native.bytesWritten - writed

        return (native.bytesWritten - writed).toInt()
    }

    actual fun flush(cursor: Cursor, output: ByteArray) {
        val readed = native.bytesRead
        val writed = native.bytesWritten
        native.deflate(output, cursor.outputOffset, cursor.outputLength, if (syncFlush) JDeflater.SYNC_FLUSH else JDeflater.NO_FLUSH)
        cursor.inputOffset += (native.bytesRead - readed).toInt()
        cursor.outputOffset += (native.bytesWritten - writed).toInt()
        _totalIn += native.bytesRead - readed
        _totalOut += native.bytesWritten - writed

    }

    actual fun end() {
        native.end()
    }

    actual val finished: Boolean
        get() = native.finished()

    actual fun finish() {
        native.finish()
    }

    private var _totalIn: Long = 0
    private var _totalOut: Long = 0

    actual val totalIn: Long
        get() = _totalIn
    actual val totalOut: Long
        get() = _totalOut

}