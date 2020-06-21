package pw.binom.compression.zlib

import pw.binom.ByteDataBuffer
import pw.binom.io.Closeable


expect class Inflater : Closeable {
    constructor()
    constructor(wrap: Boolean)

    fun end()

    @Deprecated(level = DeprecationLevel.WARNING, message = "Use Input/Output")
    fun inflate(cursor: Cursor, input: ByteArray, output: ByteArray): Int
    fun inflate(cursor: Cursor, input: ByteDataBuffer, output: ByteDataBuffer): Int
}