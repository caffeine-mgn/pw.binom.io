package pw.binom.io

interface AsyncAppendable {
    suspend fun append(c: Char): AsyncAppendable
    suspend fun append(csq: CharSequence?): AsyncAppendable
    suspend fun append(csq: CharSequence?, start: Int, end: Int): AsyncAppendable
}

interface AsyncWriter : AsyncAppendable, AsyncFlushable, AsyncCloseable
interface Writer : Appendable, Flushable, Closeable

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

inline fun Appendable.appendln(text: String) = append(text).appendln()
inline fun Appendable.appendln() = append("\n")
suspend inline fun AsyncAppendable.appendln(text: String) = append(text).appendln()
suspend inline fun AsyncAppendable.appendln() = append("\n")