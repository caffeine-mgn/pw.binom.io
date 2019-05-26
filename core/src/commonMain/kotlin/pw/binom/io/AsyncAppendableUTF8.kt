package pw.binom.io

class AsyncAppendableUTF8(private val stream: AsyncOutputStream) : AsyncAppendable {
    override suspend fun append(c: Char): AsyncAppendable {
        UTF8.write(c, stream)
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