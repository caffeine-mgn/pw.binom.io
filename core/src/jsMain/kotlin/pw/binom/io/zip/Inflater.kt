package pw.binom.io.zip

import pw.binom.io.Closeable

actual class Inflater actual constructor(wrap: Boolean) : Closeable {
    init {
        throw IllegalArgumentException("Not supported in JS")
    }

    override fun close() {
    }

    actual constructor() : this(true)

    actual fun inflate(cursor: Cursor, input: ByteArray, output: ByteArray) {
    }

}