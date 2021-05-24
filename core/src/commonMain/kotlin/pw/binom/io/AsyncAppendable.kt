package pw.binom.io

interface AsyncAppendable {
    suspend fun append(value: Char): AsyncAppendable
    suspend fun append(value: CharSequence?): AsyncAppendable
    suspend fun append(value: CharSequence?, startIndex: Int, endIndex: Int): AsyncAppendable
}

suspend fun AsyncAppendable.append(value: Boolean) = append(if (value) "true" else "false")

interface AsyncWriter : AsyncAppendable, AsyncFlushable, AsyncCloseable
interface Writer : Appendable, Flushable, Closeable

fun Appendable.asAsync() = object : AsyncAppendable {
    override suspend fun append(value: Char): AsyncAppendable {
        this@asAsync.append(value)
        return this
    }

    override suspend fun append(value: CharSequence?): AsyncAppendable {
        this@asAsync.append(value)
        return this
    }

    override suspend fun append(value: CharSequence?, startIndex: Int, endIndex: Int): AsyncAppendable {
        this@asAsync.append(value, startIndex, endIndex)
        return this
    }

}

inline fun Appendable.appendln(text: String) = append(text).appendln()
inline fun Appendable.appendln() = append("\n")
suspend inline fun AsyncAppendable.appendln(text: String) = append(text).appendln()
suspend inline fun AsyncAppendable.appendln() = append("\n")