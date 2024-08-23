package pw.binom.compression.zlib

import pw.binom.io.ByteBuffer
import pw.binom.io.Closeable
import pw.binom.io.ClosedException
import java.util.concurrent.atomic.AtomicBoolean
import java.util.zip.Inflater as JInflater

actual class Inflater actual constructor(wrap: Boolean) : Closeable {
    private val native = JInflater(!wrap)

    init {
        DeflaterMetrics.incInflaterCount()
    }

    private var closed = AtomicBoolean(false)

    private fun checkClosed() {
        if (closed.get()) {
            throw ClosedException()
        }
    }

    actual override fun close() {
        if (!closed.compareAndSet(false, true)) {
            throw ClosedException()
        }
        DeflaterMetrics.decInflaterCount()
        native.end()
    }

    actual fun end() {
        checkClosed()
        native.end()
    }

    actual fun inflate(input: ByteBuffer, output: ByteBuffer): Int {
        checkClosed()
        native.setInput(input.native)
        val writed = native.bytesWritten
        native.inflate(output.native)
        return (native.bytesWritten - writed).toInt()
    }
}
