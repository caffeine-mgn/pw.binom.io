package pw.binom.io

interface AsyncAppendable {
    suspend fun append(c: Char): AsyncAppendable
    suspend fun append(csq: CharSequence?): AsyncAppendable
    suspend fun append(csq: CharSequence?, start: Int, end: Int): AsyncAppendable
}

fun Appendable.asAsync() = object : AsyncAppendable {
    override suspend fun append(c: Char): AsyncAppendable {
        this@asAsync.append(c)
        return this
    }

    override suspend fun append(csq: CharSequence?): AsyncAppendable {
        this@asAsync.append(csq)
        return this
    }

    override suspend fun append(csq: CharSequence?, start: Int, end: Int): AsyncAppendable {
        this@asAsync.append(csq, start, end)
        return this
    }

}

