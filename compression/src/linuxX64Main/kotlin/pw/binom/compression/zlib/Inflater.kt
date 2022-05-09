package pw.binom.compression.zlib

import kotlinx.cinterop.*
import platform.posix.memset
import platform.zlib.*
import pw.binom.ByteBuffer
import pw.binom.atomic.AtomicBoolean
import pw.binom.io.Closeable
import pw.binom.io.IOException
import kotlin.native.concurrent.freeze
import kotlin.native.internal.createCleaner

@OptIn(ExperimentalStdlibApi::class)
actual class Inflater actual constructor(wrap: Boolean) : Closeable {
    internal val native =
        nativeHeap.alloc<z_stream_s>() // malloc(sizeOf<z_stream_s>().convert())!!.reinterpret<z_stream_s>()

    private var closed = AtomicBoolean(false)

    private fun checkClosed() {
        if (closed.getValue())
            throw IllegalStateException("Stream already closed")
    }

    init {
        memset(native.ptr, 0, sizeOf<z_stream_s>().convert())

        if (inflateInit2(native.ptr, if (wrap) 15 else -15) != Z_OK)
            throw IOException("inflateInit2() error")
    }

    private val cleaner = createCleaner(native) { self ->
        inflateEnd(self.ptr)
        nativeHeap.free(self)
    }

    init {
        freeze()
    }

    override fun close() {
        checkClosed()
        closed.setValue(true)
    }

    actual fun end() {
        inflateEnd(native.ptr)
    }

    actual fun inflate(input: ByteBuffer, output: ByteBuffer): Int {
        if (output.capacity == 0 || input.capacity == 0) {
            return 0
        }
        return output.refTo(output.position) { outputPtr ->
            input.refTo(input.position) { inputPtr ->
                memScoped {
                    native.avail_out = output.remaining.convert()
                    native.next_out = (outputPtr).getPointer(this).reinterpret()

                    native.avail_in = input.remaining.convert()
                    native.next_in = (inputPtr).getPointer(this).reinterpret()
                    val freeOutput = output.remaining
                    val freeInput = input.remaining
                    val r = inflate(native.ptr, Z_NO_FLUSH)
                    if (r != Z_OK && r != Z_STREAM_END)
                        throw IOException("inflate() returns [${zlibConsts(r)}]. avail_in: [${native.avail_in}], avail_out: [${native.avail_out}]")
                    val wrote = freeOutput - native.avail_out.convert<Int>()

                    input.position += freeInput - native.avail_in.convert<Int>()
                    output.position += wrote
                    return@memScoped wrote
                }
            }
        } ?: 0
    }
}
