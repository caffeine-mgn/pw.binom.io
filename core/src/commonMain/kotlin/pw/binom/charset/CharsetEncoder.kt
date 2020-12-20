package pw.binom.charset

import pw.binom.ByteBuffer
import pw.binom.CharBuffer
import pw.binom.io.Closeable

interface CharsetEncoder : Closeable {
    fun encode(input: CharBuffer, output: ByteBuffer): CharsetTransformResult
}