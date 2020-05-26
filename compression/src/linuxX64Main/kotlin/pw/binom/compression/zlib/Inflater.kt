package pw.binom.compression.zlib

import kotlinx.cinterop.*
import platform.posix.free
import platform.posix.malloc
import platform.zlib.*
import pw.binom.io.Closeable
import pw.binom.io.IOException

actual class Inflater actual constructor(wrap: Boolean) : Closeable {
    internal val native = malloc(sizeOf<z_stream_s>().convert())!!.reinterpret<z_stream_s>()

    actual constructor() : this(true)

    init {
        native.pointed.zalloc = null
        native.pointed.zfree = null
        native.pointed.opaque = null

//        inflateInit(native)
        if (inflateInit2(native, if (wrap) 15 else -15) != Z_OK)
//        if (inflateInit(native) != Z_OK)
            throw IOException("inflateInit2() error")
    }

    override fun close() {
        inflateEnd(native)
        free(native)
    }

    actual fun inflate(cursor: Cursor, input: ByteArray, output: ByteArray) {
        memScoped {
            native.pointed.avail_out = cursor.availOut.convert()
            native.pointed.next_out = output.refTo(cursor.outputOffset).getPointer(this).reinterpret()

            native.pointed.avail_in = cursor.availIn.convert()
            native.pointed.next_in = input.refTo(cursor.inputOffset).getPointer(this).reinterpret()
            val r = inflate(native, Z_NO_FLUSH)
            if (r != Z_OK && r != Z_STREAM_END)
                throw IOException("inflate() error [${zlibConsts(r)}]")


            cursor.availIn = native.pointed.avail_in.convert()
            cursor.availOut = native.pointed.avail_out.convert()
        }
    }
}