package pw.binom.compression.zlib

import pw.binom.ByteBuffer
import pw.binom.ByteDataBuffer
import pw.binom.io.Closeable


expect class Inflater : Closeable {
    constructor(wrap: Boolean=true)

    fun end()

    @Deprecated(level = DeprecationLevel.WARNING, message = "Use Input/Output")
    fun inflate(cursor: Cursor, input: ByteArray, output: ByteArray): Int
    fun inflate(cursor: Cursor, input: ByteDataBuffer, output: ByteDataBuffer): Int
    fun inflate(input: ByteBuffer, output: ByteBuffer): Int
}