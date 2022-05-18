package pw.binom.compression.zlib

import pw.binom.io.ByteBuffer
import pw.binom.io.Closeable
import java.util.zip.Deflater as JDeflater

actual class Deflater actual constructor(level: Int, wrap: Boolean, val syncFlush: Boolean) : Closeable {
    private val native = JDeflater(level, !wrap)
    override fun close() {
    }

    actual fun end() {
        native.end()
    }

    private var finishCalled = false

    actual val finished: Boolean
        get() = native.finished()

    actual fun finish() {
        finishCalled = true
        native.finish()
    }

    private var _totalIn: Long = 0
    private var _totalOut: Long = 0

    actual val totalIn: Long
        get() = _totalIn
    actual val totalOut: Long
        get() = _totalOut

    actual fun deflate(input: ByteBuffer, output: ByteBuffer): Int {
        native.setInput(input.toByteArray())
        val readedBefore = native.bytesRead
        val writedBefore = native.bytesWritten

        val outputData = ByteArray(output.remaining)
        native.deflate(outputData, 0, outputData.size, JDeflater.NO_FLUSH)

        val wasRead = (native.bytesRead - readedBefore).toInt()
        val wasWrote = (native.bytesWritten - writedBefore).toInt()
        input.position += wasRead
        output.write(outputData, 0, wasWrote)

        _totalIn += wasRead
        _totalOut += wasWrote

        return wasWrote.toInt()
    }

    actual fun flush(output: ByteBuffer): Boolean {
        if (!finishCalled) {
            return false
        }
        native.setInput(ByteArray(0))
        val readed = native.bytesRead
        val writed = native.bytesWritten
        val oo = output.toByteArray()
        val r = native.deflate(oo, 0, oo.size, if (syncFlush) JDeflater.SYNC_FLUSH else JDeflater.NO_FLUSH)
        val wasRead = (native.bytesRead - readed).toInt()
        val wasWrote = (native.bytesWritten - writed).toInt()
        output.position += wasRead
        _totalIn += wasRead
        _totalOut += wasWrote
        return !native.finished()
    }
}
