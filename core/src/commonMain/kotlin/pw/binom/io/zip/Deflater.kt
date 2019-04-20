package pw.binom.io.zip

import pw.binom.io.Closeable

expect class Deflater:Closeable {
    constructor()
    constructor(level: Int, wrap: Boolean)

    fun deflate(cursor: Cursor, input: ByteArray, output: ByteArray)
    fun flush(cursor: Cursor,output: ByteArray)
}