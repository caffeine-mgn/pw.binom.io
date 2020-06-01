package pw.binom.compression.zlib

import pw.binom.io.Closeable

expect class Deflater : Closeable {
    constructor()
    constructor(level: Int, wrap: Boolean, syncFlush: Boolean)

    val totalIn: Long
    val totalOut: Long

    fun end()
    val finished: Boolean
    fun finish()

    fun deflate(cursor: Cursor, input: ByteArray, output: ByteArray): Int
    fun flush(cursor: Cursor, output: ByteArray)
}