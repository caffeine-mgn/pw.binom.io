package pw.binom.io

import kotlin.jvm.JvmName

interface AsyncAppendable {
    suspend fun append(value: CharSequence?): AsyncAppendable
    suspend fun append(value: Char): AsyncAppendable
    suspend fun append(value: Boolean): AsyncAppendable = append(value.toString())
    suspend fun append(value: Byte): AsyncAppendable = append(value.toString())
    suspend fun append(value: Short): AsyncAppendable = append(value.toString())
    suspend fun append(value: Int): AsyncAppendable = append(value.toString())
    suspend fun append(value: Long): AsyncAppendable = append(value.toString())
    suspend fun append(value: UByte): AsyncAppendable = append(value.toString())
    suspend fun append(value: UShort): AsyncAppendable = append(value.toString())
    suspend fun append(value: UInt): AsyncAppendable = append(value.toString())
    suspend fun append(value: ULong): AsyncAppendable = append(value.toString())
    suspend fun append(value: Float): AsyncAppendable = append(value.toString())
    suspend fun append(value: Double): AsyncAppendable = append(value.toString())
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
