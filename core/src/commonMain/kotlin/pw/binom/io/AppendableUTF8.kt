package pw.binom.io

import pw.binom.ByteDataBuffer
import pw.binom.Output
import pw.binom.tmp8

class AppendableUTF8(private val stream: Output) : Appendable {
    //private val data = ByteDataBuffer.alloc(4)
    override fun append(c: Char): AppendableUTF8 {
        tmp8.clear()
        val r = UTF8.unicodeToUtf8(c, tmp8)
        tmp8.flip()
        stream.write(tmp8)
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

fun Output.utf8Appendable() = AppendableUTF8(this)