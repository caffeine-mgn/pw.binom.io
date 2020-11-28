package pw.binom.io

import pw.binom.ByteBuffer
import pw.binom.Output

class AppendableUTF8(private val stream: Output) : Writer {
    private val data = ByteBuffer.alloc(4)
    override fun append(c: Char): AppendableUTF8 {
        data.clear()
        val r = UTF8.unicodeToUtf8(c, data)
        data.flip()
        stream.write(data)
        return this
    }

    override fun append(csq: CharSequence?): AppendableUTF8 {
        csq?.forEach {
            append(it)
        }
        return this
    }

    override fun append(csq: CharSequence?, start: Int, end: Int): AppendableUTF8 {
        csq ?: return this
        (start..end).forEach {
            append(csq[it])
        }
        return this
    }

    override fun flush() {
        stream.flush()
    }

    override fun close() {
        data.close()
        stream.close()
    }
}

fun Output.utf8Appendable() = AppendableUTF8(this)