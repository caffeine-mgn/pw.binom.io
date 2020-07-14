package pw.binom.compression.zlib

import pw.binom.ByteBuffer
import pw.binom.ByteDataBuffer
import pw.binom.io.Closeable


expect class Inflater : Closeable {
    constructor(wrap: Boolean = true)

    fun end()

    fun inflate(input: ByteBuffer, output: ByteBuffer): Int
}