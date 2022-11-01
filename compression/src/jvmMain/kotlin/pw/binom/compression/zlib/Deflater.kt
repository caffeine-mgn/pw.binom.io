package pw.binom.compression.zlib

import pw.binom.io.ByteBuffer
import pw.binom.io.Closeable
import pw.binom.io.ClosedException
import java.util.concurrent.atomic.AtomicBoolean
import java.util.zip.Deflater as JDeflater

actual class Deflater actual constructor(level: Int, wrap: Boolean, val syncFlush: Boolean) : Closeable {
    init {
        DeflaterMetrics.incDeflaterCount()
    }

    private val native = JDeflater(level, !wrap)

    private var closed = AtomicBoolean(false)

    private fun checkClosed() {
        if (closed.get()) {
            throw ClosedException()
        }
    }

    override fun close() {
        if (!closed.compareAndSet(false, true)) {
            throw ClosedException()
        }
        DeflaterMetrics.decDeflaterCount()
    }

    actual fun end() {
        checkClosed()
        native.end()
    }

    private var finishCalled = false

    actual val finished: Boolean
        get() = native.finished()

    actual fun finish() {
        checkClosed()
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
        checkClosed()
        native.setInput(input.native)
        val readedBefore = native.bytesRead
        val writedBefore = native.bytesWritten
        native.deflate(output.native, JDeflater.NO_FLUSH)

        val wroteAfter = native.bytesWritten - writedBefore
        _totalIn += native.bytesRead - readedBefore
        _totalOut += wroteAfter

        return wroteAfter.toInt()
    }

    actual fun flush(output: ByteBuffer): Boolean {
        checkClosed()
        if (!finishCalled) {
            return false
        }
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

private val EMPTY_BUFFER = java.nio.ByteBuffer.allocate(0)
