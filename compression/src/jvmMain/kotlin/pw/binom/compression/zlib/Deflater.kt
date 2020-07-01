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
        while (true) {
            val readed = native.bytesRead
            val writed = native.bytesWritten
            val r = native.deflate(output, cursor.outputOffset, cursor.availOut, if (syncFlush) JDeflater.SYNC_FLUSH else JDeflater.NO_FLUSH)
            cursor.inputOffset += (native.bytesRead - readed).toInt()
            cursor.outputOffset += (native.bytesWritten - writed).toInt()
            _totalIn += native.bytesRead - readed
            _totalOut += native.bytesWritten - writed
            if (r <= 0)
                break
        }
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

//    private var _finished = false

    actual fun deflate(cursor: Cursor, input: ByteDataBuffer, output: ByteDataBuffer): Int {
        return input.update(cursor.inputOffset, cursor.inputLength) { inputBuff ->
            output.update(cursor.outputOffset, cursor.outputLength) { outputBuff ->
                native.setInput(inputBuff)
//                if (!_finished && !native.needsInput()) {
//                    _finished = true
//                    native.finish()
//                }
                val readed = native.bytesRead
                val writed = native.bytesWritten
                val mode = if (syncFlush) JDeflater.SYNC_FLUSH else JDeflater.NO_FLUSH
                val defResult = native.deflate(outputBuff, mode)

                val wasRead = (native.bytesRead - readed).toInt()
                val wasWrote = (native.bytesWritten - writed).toInt()

                cursor.inputOffset += wasRead
//                cursor.inputLength -= (native.bytesRead - readed).toInt()

                cursor.outputOffset += wasWrote
//                cursor.outputLength -= (native.bytesWritten - writed).toInt()
                native.setInput(EMPTY_BUFFER)
                _totalIn += native.bytesRead - readed
                _totalOut += native.bytesWritten - writed
                (native.bytesWritten - writed).toInt()
            }
        }
    }

    actual fun flush(cursor: Cursor, output: ByteDataBuffer): Boolean {
        if (!finishCalled)
            return false
//        var flag = true
//        while (flag) {
        return output.update(cursor.outputOffset, cursor.availOut) { output ->
            native.setInput(EMPTY_BUFFER)
            val readed = native.bytesRead
            val writed = native.bytesWritten
            val r = native.deflate(output, if (syncFlush) JDeflater.SYNC_FLUSH else JDeflater.NO_FLUSH)
            val wasRead = (native.bytesRead - readed).toInt()
            val wasWrote = (native.bytesWritten - writed).toInt()
            cursor.inputOffset += wasRead
            cursor.outputOffset += wasWrote
            _totalIn += wasRead
            _totalOut += wasWrote
            !native.finished()
        }
    }

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