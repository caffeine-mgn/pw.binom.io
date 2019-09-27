package pw.binom.io.zip

import pw.binom.io.Closeable

actual class Deflater actual constructor(level: Int, wrap: Boolean) : Closeable {
    init {
        throw IllegalArgumentException("Not supported in JS")
    }

    override fun close() {
    }

    actual constructor() : this(6, true)

    actual fun deflate(cursor: Cursor, input: ByteArray, output: ByteArray) {
    }

    actual fun flush(cursor: Cursor, output: ByteArray) {
    }

}