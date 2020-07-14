package pw.binom.compression.zlib

import kotlinx.cinterop.*
import platform.posix.memset
import platform.zlib.*
import pw.binom.ByteBuffer
import pw.binom.ByteDataBuffer
import pw.binom.io.Closeable
import pw.binom.io.IOException

actual class Deflater actual constructor(level: Int, wrap: Boolean, val syncFlush: Boolean) : Closeable {

    internal val native = nativeHeap.alloc<z_stream_s>()

    init {
        memset(native.ptr, 0, sizeOf<z_stream_s>().convert())
        if (deflateInit2(native.ptr, level.convert(), Z_DEFLATED, if (wrap) 15 else -15, 8, Z_DEFAULT_STRATEGY) != Z_OK)
            throw IOException("deflateInit() error")
    }

    private var closed = false

    private fun checkClosed() {
        if (closed)
            throw IllegalStateException("Stream already closed")
    }

    override fun close() {
        checkClosed()
        val vv = deflateEnd(native.ptr)
        nativeHeap.free(native)
        closed = true
    }

    actual fun end() {
        checkClosed()
        deflateEnd(native.ptr)
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

    actual fun deflate(input: ByteBuffer, output: ByteBuffer): Int {
        checkClosed()
        if (output.remaining == 0)
            throw IllegalArgumentException("Output Buffer has no Free Space")
        if (!_finishing && input.remaining == 0)
            return 0
        native.next_out = (output.native + output.position)!!.reinterpret()
        native.avail_out = output.remaining.convert()

        native.next_in = (input.native + input.position)!!.reinterpret()
        native.avail_in = input.remaining.convert()
        val freeOutput = output.remaining
        val freeInput = input.remaining

        val mode = if (_finishing)
            Z_FINISH
//            Z_SYNC_FLUSH
//            Z_FULL_FLUSH
        else
            Z_NO_FLUSH
        val deflateResult = deflate(native.ptr, mode)
        if (deflateResult != Z_OK)
            throw IOException("deflate() returns [${zlibConsts(deflateResult)}]. avail_in: [${native.avail_in}], avail_out: [${native.avail_out}]")
        val wrote = freeOutput - native.avail_out.convert<Int>()
        input.position += freeInput - native.avail_in.convert<Int>()
        output.position += wrote

        val outLength = freeOutput - output.remaining
        val inLength = freeInput - input.remaining
        _totalOut += outLength
        _totalIn += inLength
        if (_finishing)
            _finished = true
        return outLength
    }

    actual fun flush(output: ByteBuffer): Boolean {
        if (!_finishing)
            return false
        while (true) {
            val writed = output.remaining
            val mode = if (_finishing)
                Z_FINISH
            else
                if (syncFlush)
                    Z_SYNC_FLUSH
                else
                    Z_NO_FLUSH

            val r = memScoped {
                native.next_out = (output.native + output.position)!!.reinterpret()
                native.avail_out = output.remaining.convert()

                native.next_in = null
                native.avail_in = 0.convert()
                deflate(native.ptr, mode)
            }
//            if (r != Z_OK)
//                throw IOException("Can't flush data. Code: $r (${zlibConsts(r)})")

            val outLength = writed - native.avail_out.convert<Int>()
            output.position += writed - native.avail_out.convert<Int>()

            _totalOut += outLength

            if (r == Z_OK || r == Z_BUF_ERROR)
                return true
            if (r == Z_STREAM_END)
                return false
        }
    }

}