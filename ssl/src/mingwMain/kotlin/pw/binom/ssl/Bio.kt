package pw.binom.ssl

import kotlinx.cinterop.*
import platform.openssl.*
import platform.posix.size_tVar
import pw.binom.DEFAULT_BUFFER_SIZE
import pw.binom.io.Closeable
import pw.binom.io.InputStream
import pw.binom.io.OutputStream

inline class Bio(val self: CPointer<BIO>) : Closeable {
    fun read(data: ByteArray, offset: Int = 0, length: Int = data.size - offset): Int =
            memScoped {
                val readed = alloc<size_tVar>()
                if (BIO_read_ex(self, data.refTo(offset), length.convert(), readed.ptr) < 0)
                    TODO()
                readed.value.toInt()
            }

    fun write(data: ByteArray, offset: Int = 0, length: Int = data.size - offset): Int =
            memScoped {
                val wrote = alloc<size_tVar>()
                if (BIO_write_ex(self, data.refTo(offset), length.convert(), wrote.ptr) < 0)
                    TODO()
                wrote.value.toInt()
            }

    override fun close() {
        BIO_free(self)
    }

    var cursor: Int
        get() = BIO_ctrl(self, BIO_C_FILE_TELL.convert(), 0.convert(), null).convert()
        set(value) {
            BIO_ctrl(self, BIO_C_FILE_SEEK.convert(), value.convert(), null)
        }

    val eof: Boolean
        get() = BIO_ctrl(self, BIO_CTRL_EOF.convert(), 0, null).convert<Int>() == 1

    fun reset() {
        if (BIO_ctrl(self, BIO_CTRL_RESET, 0, null) < 0)
            TODO()
    }

    fun copyTo(stream: Bio, bufferLength: Int = DEFAULT_BUFFER_SIZE) {
        val buf = ByteArray(bufferLength)
        while (!eof) {
            val len = read(buf)
            if (len > 0)
                stream.write(data = buf, length = len)
        }
    }

    fun copyTo(stream: OutputStream, bufferLength: Int = DEFAULT_BUFFER_SIZE) {
        val buf = ByteArray(bufferLength)
        while (!eof) {
            val len = read(buf)
            if (len > 0)
                stream.write(data = buf, length = len)
        }
    }

    companion object {
        fun mem() = Bio(BIO_new(BIO_s_mem())!!)
        fun mem(size: Int): Bio {
            val ptr = platform.posix.malloc(size.convert())
            val bio = BIO_new_mem_buf(ptr, size)!!
            if (BIO_ctrl(bio, BIO_CTRL_SET_CLOSE, BIO_CLOSE, null) < 0)
                TODO()
            return Bio(bio)
        }

        fun mem(data:ByteArray): Bio {
            val bio = BIO_new_mem_buf(data.refTo(0), data.size)!!
            if (BIO_ctrl(bio, BIO_CTRL_SET_CLOSE, BIO_NOCLOSE, null) < 0)
                TODO()
            return Bio(bio)
        }
    }
}

fun InputStream.copyTo(bio: Bio, bufferLength: Int = DEFAULT_BUFFER_SIZE) {
    val buf = ByteArray(bufferLength)
    while (true) {
        val l = read(buf)
        if (l == 0)
            return
        bio.write(buf, l)
    }
}