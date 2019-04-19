package pw.binom.io.zip

import kotlinx.cinterop.*
import platform.posix.free
import platform.posix.malloc
import platform.zlib.*
import pw.binom.io.Closeable
import pw.binom.io.IOException

actual class Deflater actual constructor(level: Int, wrap: Boolean) : Closeable {

    internal val native = malloc(sizeOf<z_stream_s>().convert())!!.reinterpret<z_stream_s>()

    init {
        if (deflateInit2(native, level.convert(), Z_DEFLATED, if (wrap) 15 else -15, 8, Z_DEFAULT_STRATEGY) != Z_OK)
            throw IOException("deflateInit() error")
    }

    override fun close() {
        deflateEnd(native)
        free(native)
    }

    actual constructor() : this(6, true)

    actual fun deflate(cursor: Cursor, input: ByteArray, output: ByteArray) {
        memScoped {
            native.pointed.next_out = output.refTo(cursor.outputOffset).getPointer(this).reinterpret()
            native.pointed.avail_out = cursor.availOut.convert()

            native.pointed.next_in = input.refTo(cursor.inputOffset).getPointer(this).reinterpret()
            native.pointed.avail_in = cursor.availIn.convert()

            if (deflate(native, Z_NO_FLUSH) != Z_OK)
                throw IOException("deflate() error")

            cursor.availIn = native.pointed.avail_in.convert()
            cursor.availOut = native.pointed.avail_out.convert()
        }
    }

    actual fun flush(cursor: Cursor, output: ByteArray) {
        memScoped {
            native.pointed.avail_out = cursor.availOut.convert()
            native.pointed.next_out = output.refTo(cursor.outputOffset).getPointer(this).reinterpret()
            if (deflate(native, Z_SYNC_FLUSH) != Z_OK)
                throw IOException("deflate() error")

            cursor.availOut = native.pointed.avail_out.convert()
        }

    }

}