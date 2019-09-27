package pw.binom.io

class AppendableUTF8(private val stream: OutputStream) : Appendable {
    private val data = ByteArray(6)
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

fun OutputStream.utf8Appendable() = AppendableUTF8(this)