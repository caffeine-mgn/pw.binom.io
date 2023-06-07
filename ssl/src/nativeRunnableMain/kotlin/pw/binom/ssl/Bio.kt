package pw.binom.ssl

import kotlinx.cinterop.*
import platform.openssl.*
import pw.binom.DEFAULT_BUFFER_SIZE
import pw.binom.io.ByteBuffer
import pw.binom.io.Closeable
import pw.binom.io.Output
import pw.binom.io.use

value class Bio(val self: CPointer<BIO>) : Closeable {
    fun read(data: ByteArray, offset: Int = 0, length: Int = data.size - offset): Int =
        memScoped {
            val r = data.usePinned { data ->
                BIO_read(self, data.addressOf(offset), length.convert())
            }
            if (r < 0) {
                TODO()
            }
            r
        }

    val ptr
        get() = memScoped {
            val ptr = allocPointerTo<ByteVar>()
            BIO_ctrl(self, BIO_C_GET_BUF_MEM_PTR, 0, ptr.reinterpret())
            ptr.value!!
        }

    fun toByteArray(): ByteArray {
        val c = cursor
        try {
            cursor = 0
            val bytes = ByteArray(size)
            read(bytes)
            return bytes
        } finally {
            cursor = c
        }
    }

    fun read(data: ByteBuffer): Int {
        if (!data.isReferenceAccessAvailable()) {
            return 0
        }
        return memScoped {
            val r = data.ref(0) { dataPtr, remaining ->
                BIO_read(self, dataPtr, remaining.convert())
            }
            if (r < 0) {
                TODO()
            }
            data.position += r
            r
        }
    }

    fun write(data: ByteArray, offset: Int = 0, length: Int = data.size - offset): Int =
        memScoped {
            val r = data.usePinned { data ->
                BIO_write(self, data.addressOf(offset), length.convert())
            }
            if (r < 0) {
                TODO()
            }
            r
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

    val size: Int
        get() = BIO_ctrl(self, BIO_CTRL_INFO, 0, null).convert()

    fun push(bio: Bio): Bio {
        BIO_push(self, bio.self)
        return this
    }

    fun pop() = BIO_pop(self)?.let { Bio(it) }

    fun reset() {
        if (BIO_ctrl(self, BIO_CTRL_RESET, 0, null) < 0) {
            TODO("BIO_ctrl(BIO_CTRL_RESET) is fail")
        }
    }

    fun copyTo(stream: Bio, bufferLength: Int = DEFAULT_BUFFER_SIZE) {
        val buf = ByteArray(bufferLength)
        while (!eof) {
            val len = read(buf)
            if (len > 0) {
                stream.write(data = buf, length = len)
            }
        }
    }

    fun copyTo(stream: Output, bufferLength: Int = DEFAULT_BUFFER_SIZE) {
        ByteBuffer(bufferLength).use { buf ->
            while (!eof) {
                buf.clear()
                read(buf)
                buf.flip()
                stream.write(data = buf)
            }
        }
    }

    companion object {
        fun mem() = Bio(BIO_new(BIO_s_mem())!!)
        fun mem(size: Int): Bio {
            val ptr = platform.posix.malloc(size.convert())
            val bio = BIO_new_mem_buf(ptr, size)!!
            if (BIO_ctrl(bio, BIO_CTRL_SET_CLOSE, BIO_CLOSE.convert(), null) < 0) {
                TODO()
            }
            return Bio(bio)
        }

        fun mem(data: ByteArray): Bio {
            val bio = data.usePinned { pinnedData ->
                BIO_new_mem_buf(pinnedData.addressOf(0), data.size)!!
            }
            if (BIO_ctrl(bio, BIO_CTRL_SET_CLOSE, BIO_NOCLOSE.convert(), null) < 0) {
                TODO()
            }
            return Bio(bio)
        }
    }
}
