package pw.binom.io

import pw.binom.ByteDataBuffer
import pw.binom.Output

class AppendableUTF8(private val stream: OutputStream) : Appendable {
    private val data = ByteArray(4)
    override fun append(c: Char): AppendableUTF8 {
        stream.write(data, 0, UTF8.unicodeToUtf8(c, data))
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
}

class AppendableUTF82(private val stream: Output) : Appendable {
    private val data = ByteDataBuffer.alloc(4)
    override fun append(c: Char): AppendableUTF82 {
        val r = UTF8.unicodeToUtf8(c, data)
        stream.write(data, 0, r)
        return this
    }

    override fun append(csq: CharSequence?): AppendableUTF82 {
        csq?.forEach {
            append(it)
        }
        return this
    }

    override fun append(csq: CharSequence?, start: Int, end: Int): AppendableUTF82 {
        csq ?: return this
        (start..end).forEach {
            append(csq[it])
        }
        return this
    }
}

fun OutputStream.utf8Appendable() = AppendableUTF8(this)
fun Output.utf8Appendable() = AppendableUTF82(this)