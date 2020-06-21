package pw.binom.compression.zlib

import pw.binom.ByteDataBuffer
import pw.binom.io.Closeable

expect class Deflater : Closeable {
    constructor()
    constructor(level: Int, wrap: Boolean, syncFlush: Boolean)

    val totalIn: Long
    val totalOut: Long

    fun end()
    val finished: Boolean
    fun finish()

    @Deprecated(level = DeprecationLevel.WARNING, message = "Use Input/Output")
    fun deflate(cursor: Cursor, input: ByteArray, output: ByteArray): Int
    fun deflate(cursor: Cursor, input: ByteDataBuffer, output: ByteDataBuffer): Int

    @Deprecated(level = DeprecationLevel.WARNING, message = "Use Input/Output")
    fun flush(cursor: Cursor, output: ByteArray)

    /**
     * Flush changes
     *
     * @return true - you must recall this method again
     */
    fun flush(cursor: Cursor, output: ByteDataBuffer): Boolean
}