package pw.binom.compression.zlib

import pw.binom.ByteBuffer
import pw.binom.io.Closeable

expect class Deflater : Closeable {
    constructor(level: Int=6, wrap: Boolean=true, syncFlush: Boolean=true)

    val totalIn: Long
    val totalOut: Long

    fun end()
    val finished: Boolean
    fun finish()

    fun deflate(input: ByteBuffer, output: ByteBuffer): Int

    /**
     * Flush changes
     *
     * @return true - you must recall this method again
     */
    fun flush(output: ByteBuffer): Boolean
}