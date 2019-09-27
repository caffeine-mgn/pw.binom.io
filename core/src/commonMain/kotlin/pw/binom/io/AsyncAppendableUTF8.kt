package pw.binom.io

class AsyncAppendableUTF8(private val stream: AsyncOutputStream) : AsyncAppendable {
    private val data = ByteArray(6)
    override suspend fun append(c: Char): AsyncAppendable {
        stream.write(data, 0, UTF8.unicodeToUtf8(c, data))
        return this
    }

    override suspend fun append(csq: CharSequence?): AsyncAppendable {
        csq?.forEach {
            append(it)
        }
        return this
    }

    override suspend fun append(csq: CharSequence?, start: Int, end: Int): AsyncAppendable {
        csq ?: return this
        (start..end).forEach {
            append(csq[it])
        }
        return this
    }

}

fun AsyncOutputStream.utf8Appendable() = AsyncAppendableUTF8(this)