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

suspend inline fun AsyncAppendable.appendLine(text: String) = append(text).appendLine()
suspend inline fun AsyncAppendable.appendLine() = append("\n")