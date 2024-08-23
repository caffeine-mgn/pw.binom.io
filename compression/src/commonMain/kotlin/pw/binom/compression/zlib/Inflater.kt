package pw.binom.compression.zlib

import pw.binom.io.ByteBuffer
import pw.binom.io.Closeable

expect class Inflater : Closeable {
    constructor(wrap: Boolean = true)

    fun end()

    fun inflate(input: ByteBuffer, output: ByteBuffer): Int
  override fun close()
}
