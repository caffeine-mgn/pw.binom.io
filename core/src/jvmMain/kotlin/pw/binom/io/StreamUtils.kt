package pw.binom.io

import java.io.InputStream as JInputStream
import java.io.OutputStream as JOutputStream


fun JInputStream.wrap() = object : InputStream {
    override fun read(data: ByteArray, offset: Int, length: Int): Int = this@wrap.read(data, offset, length)

    override fun close() {
        this@wrap.close()
    }

    override val available: Int
        get() = this@wrap.available()
}

fun JOutputStream.wrap() = object : OutputStream {
    override fun write(data: ByteArray, offset: Int, length: Int): Int {
        this@wrap.write(data, offset, length)
        return length
    }

    override fun flush() {
        this@wrap.flush()
    }

    override fun close() {
        this@wrap.close()
    }

}