package pw.binom.compression.zlib

import pw.binom.ByteDataBuffer
import pw.binom.io.Closeable
import pw.binom.update
import java.nio.ByteBuffer
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

    actual fun deflate(input: pw.binom.ByteBuffer, output: pw.binom.ByteBuffer): Int {
        native.setInput(input.native)
        val readedBefore = native.bytesRead
        val writedBefore = native.bytesWritten
        native.deflate(output.native, JDeflater.NO_FLUSH)

        val wroteAfter = native.bytesWritten - writedBefore
        _totalIn += native.bytesRead - readedBefore
        _totalOut += wroteAfter

        return wroteAfter.toInt()
    }

    actual fun flush(output: pw.binom.ByteBuffer): Boolean {
        if (!finishCalled)
            return false
        native.setInput(EMPTY_BUFFER)
        val readed = native.bytesRead
        val writed = native.bytesWritten
        val r = native.deflate(output.native, if (syncFlush) JDeflater.SYNC_FLUSH else JDeflater.NO_FLUSH)
        val wasRead = (native.bytesRead - readed).toInt()
        val wasWrote = (native.bytesWritten - writed).toInt()
        _totalIn += wasRead
        _totalOut += wasWrote
        return !native.finished()
    }
}

private val EMPTY_BUFFER = ByteBuffer.allocate(0)