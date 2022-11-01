package pw.binom.compression.zlib

import kotlinx.cinterop.*
import platform.posix.memset
import platform.zlib.*
import pw.binom.atomic.AtomicBoolean
import pw.binom.atomic.AtomicLong
import pw.binom.io.ByteBuffer
import pw.binom.io.Closeable
import pw.binom.io.ClosedException
import pw.binom.io.IOException
import kotlin.native.internal.createCleaner

@OptIn(ExperimentalStdlibApi::class)
actual class Deflater actual constructor(level: Int, wrap: Boolean, val syncFlush: Boolean) : Closeable {

    internal val native = nativeHeap.alloc<z_stream_s>()
    private var closed = AtomicBoolean(false)

    private val _totalIn = AtomicLong(0)
    private val _totalOut = AtomicLong(0)

    actual val totalIn: Long
        get() = _totalIn.getValue()
    actual val totalOut: Long
        get() = _totalOut.getValue()

    private var _finishing = AtomicBoolean(false)
    private var _finished = AtomicBoolean(false)
    actual val finished: Boolean
        get() = _finished.getValue()

    init {
        DeflaterMetrics.incDeflaterCount()
        memset(native.ptr, 0, sizeOf<z_stream_s>().convert())
        if (deflateInit2(
                native.ptr,
                level.convert(),
                Z_DEFLATED,
                if (wrap) 15 else -15,
                8,
                Z_DEFAULT_STRATEGY
            ) != Z_OK
        ) {
            throw IOException("deflateInit() error")
        }
    }

    private val cleaner = createCleaner(native) { self ->
        deflateEnd(self.ptr)
        nativeHeap.free(self)
    }

    private fun checkClosed() {
        if (closed.getValue()) {
            throw ClosedException()
        }
    }

    override fun close() {
        if (!closed.compareAndSet(false, true)) {
            throw ClosedException()
        }
        DeflaterMetrics.decDeflaterCount()
        closed.setValue(true)
    }

    actual fun end() {
        checkClosed()
        deflateEnd(native.ptr)
    }

    actual fun finish() {
        checkClosed()
        _finishing.setValue(true)
    }

    actual fun deflate(input: ByteBuffer, output: ByteBuffer): Int {
        return output.refTo(output.position) { outputPtr ->
            input.refTo(input.position) { inputPtr ->
                memScoped {
                    checkClosed()
                    if (output.remaining == 0) {
                        throw IllegalArgumentException("Output Buffer has no Free Space")
                    }
                    if (!_finishing.getValue() && input.remaining == 0) {
                        return@memScoped 0
                    }
                    native.next_out = (outputPtr.getPointer(this)).reinterpret()
                    native.avail_out = output.remaining.convert()

                    native.next_in = (inputPtr).getPointer(this).reinterpret()
                    native.avail_in = input.remaining.convert()
                    val freeOutput = output.remaining
                    val freeInput = input.remaining

                    val mode = if (_finishing.getValue()) {
                        Z_FINISH
                    }
//            Z_SYNC_FLUSH
//            Z_FULL_FLUSH
                    else {
                        Z_NO_FLUSH
                    }
                    val deflateResult = deflate(native.ptr, mode)
                    if (deflateResult != Z_OK) {
                        throw IOException("deflate() returns [${zlibConsts(deflateResult)}]. avail_in: [${native.avail_in}], avail_out: [${native.avail_out}]")
                    }
                    val wrote = freeOutput - native.avail_out.convert<Int>()
                    input.position += freeInput - native.avail_in.convert<Int>()
                    output.position += wrote

                    val outLength = freeOutput - output.remaining
                    val inLength = freeInput - input.remaining
                    _totalOut.addAndGet(outLength.toLong())
                    _totalIn.addAndGet(inLength.toLong())
                    if (_finishing.getValue()) {
                        _finished.setValue(true)
                    }
                    return@memScoped outLength
                }
            }
        } ?: 0
    }

    actual fun flush(output: ByteBuffer): Boolean {
        if (output.remaining == 0) {
            return false
        }
        if (!_finishing.getValue()) {
            return false
        }
        while (true) {
            val writed = output.remaining

            val mode = when {
                _finishing.getValue() -> Z_FINISH
                syncFlush -> Z_SYNC_FLUSH
                else -> Z_NO_FLUSH
            }

            val r = output.refTo(output.position) { outputPtr ->
                memScoped {
                    native.next_out = outputPtr.getPointer(this).reinterpret()
                    native.avail_out = output.remaining.convert()

                    native.next_in = null
                    native.avail_in = 0.convert()
                    deflate(native.ptr, mode)
                }
            } ?: 0

//            if (r != Z_OK)
//                throw IOException("Can't flush data. Code: $r (${zlibConsts(r)})")

            val outLength = writed - native.avail_out.convert<Int>()
            output.position += writed - native.avail_out.convert<Int>()

            _totalOut.addAndGet(outLength.toLong())

            if (r == Z_OK || r == Z_BUF_ERROR) {
                return true
            }
            if (r == Z_STREAM_END) {
                return false
            }
        }
    }
}
