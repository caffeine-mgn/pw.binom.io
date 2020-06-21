package pw.binom.compression.zlib

import kotlinx.cinterop.*
import platform.posix.free
import platform.posix.malloc
import platform.posix.memset
import platform.zlib.*
import pw.binom.ByteDataBuffer
import pw.binom.io.Closeable
import pw.binom.io.IOException

actual class Deflater actual constructor(level: Int, wrap: Boolean, val syncFlush: Boolean) : Closeable {

    internal val native = malloc(sizeOf<z_stream_s>().convert())!!.reinterpret<z_stream_s>()

    init {
        memset(native, 0, sizeOf<z_stream_s>().convert())
        if (deflateInit2(native, level.convert(), Z_DEFLATED, if (wrap) 15 else -15, 8, Z_DEFAULT_STRATEGY) != Z_OK)
            throw IOException("deflateInit() error")
    }

    private var closed = false

    private fun checkClosed() {
        if (closed)
            throw IllegalStateException("Stream already closed")
    }

    override fun close() {
        checkClosed()
        val vv = deflateEnd(native)
        free(native)
        closed = true
    }

    actual constructor() : this(6, true, true)

    actual fun deflate(cursor: Cursor, input: ByteArray, output: ByteArray): Int {
        checkClosed()
        memScoped {
            native.pointed.next_out = output.refTo(cursor.outputOffset).getPointer(this).reinterpret()
            native.pointed.avail_out = cursor.availOut.convert()

            native.pointed.next_in = input.refTo(cursor.inputOffset).getPointer(this).reinterpret()
            native.pointed.avail_in = cursor.availIn.convert()
            val writed = cursor.availOut
            val readed = cursor.availIn

            val mode = if (_finishing)
                Z_FINISH
//            Z_SYNC_FLUSH
//            Z_FULL_FLUSH
            else
                Z_NO_FLUSH
            if (deflate(native, mode) != Z_OK)
                throw IOException("deflate() error")
            cursor.availIn = native.pointed.avail_in.convert()
            cursor.availOut = native.pointed.avail_out.convert()
            val outLength = writed - cursor.availOut
            val inLength = readed - cursor.availIn
            _totalOut += outLength
            _totalIn += inLength
            if (_finishing)
                _finished = true
            return outLength
        }
    }

    actual fun flush(cursor: Cursor, output: ByteArray) {
        checkClosed()
//        if (cursor.availIn == 0)
//            return
        memScoped {
            native.pointed.avail_in = 0u
            native.pointed.avail_out = cursor.availOut.convert()
            native.pointed.next_out = output.refTo(cursor.outputOffset).getPointer(this).reinterpret()
            val writed = cursor.availOut
            val mode = when {
                _finishing -> Z_FINISH
                syncFlush -> Z_SYNC_FLUSH
                !syncFlush -> Z_NO_FLUSH
                else -> TODO()
            }
            val code = deflate(native, mode)
            if (_finishing && (code == Z_OK || code == Z_STREAM_END))
                _finished = false

            if (code != Z_OK && code != Z_BUF_ERROR && code != Z_STREAM_END)
                throw IOException("deflate() error $code")

            cursor.availOut = native.pointed.avail_out.convert()
            val outLength = writed - cursor.availOut
            _totalOut += outLength
        }
    }

    actual fun end() {
        checkClosed()
        deflateEnd(native)
    }

    private var _totalIn: Long = 0
    private var _totalOut: Long = 0

    actual val totalIn: Long
        get() = _totalIn
    actual val totalOut: Long
        get() = _totalOut

    private var _finishing: Boolean = false
    private var _finished: Boolean = false
    actual val finished: Boolean
        get() = _finished

    actual fun finish() {
        checkClosed()
        _finishing = true
    }

    actual fun deflate(cursor: Cursor, input: ByteDataBuffer, output: ByteDataBuffer): Int {
        checkClosed()
        memScoped {
            native.pointed.next_out = output.refTo(cursor.outputOffset).getPointer(this).reinterpret()
            native.pointed.avail_out = cursor.availOut.convert()

            native.pointed.next_in = input.refTo(cursor.inputOffset).getPointer(this).reinterpret()
            native.pointed.avail_in = cursor.availIn.convert()
            val writed = cursor.availOut
            val readed = cursor.availIn

            val mode = if (_finishing)
                Z_FINISH
            else
                if (syncFlush)
                    Z_SYNC_FLUSH
                else
                    Z_NO_FLUSH
            if (deflate(native, mode) != Z_OK)
                throw IOException("deflate() error")
            cursor.availIn = native.pointed.avail_in.convert()
            cursor.availOut = native.pointed.avail_out.convert()
            val outLength = writed - cursor.availOut
            val inLength = readed - cursor.availIn
            _totalOut += outLength
            _totalIn += inLength
            if (_finishing)
                _finished = true
            return outLength
        }
    }

    actual fun flush(cursor: Cursor, output: ByteDataBuffer): Boolean {
        if (!_finishing)
            return false
        while (true) {
            val writed = cursor.availOut
            val readed = cursor.availIn
            val mode = if (_finishing)
                Z_FINISH
            else
                if (syncFlush)
                    Z_SYNC_FLUSH
                else
                    Z_NO_FLUSH

            val r = memScoped {
                native.pointed.next_out = output.refTo(cursor.outputOffset).getPointer(this).reinterpret()
                native.pointed.avail_out = cursor.availOut.convert()

                native.pointed.next_in = null
                native.pointed.avail_in = cursor.availIn.convert()
                deflate(native, mode)
            }
//            if (r != Z_OK)
//                throw IOException("Can't flush data. Code: $r (${zlibConsts(r)})")

            cursor.availIn = native.pointed.avail_in.convert()
            cursor.availOut = native.pointed.avail_out.convert()

            val outLength = writed - cursor.availOut
            val inLength = readed - cursor.availIn

            _totalIn += inLength
            _totalOut += outLength

            if (r == Z_OK || r == Z_BUF_ERROR)
                return true
            if (r == Z_STREAM_END)
                return false
        }
    }

}