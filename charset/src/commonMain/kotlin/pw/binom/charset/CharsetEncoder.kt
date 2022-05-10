package pw.binom.charset

import pw.binom.CharBuffer
import pw.binom.io.ByteBuffer
import pw.binom.io.Closeable

interface CharsetEncoder : Closeable {
    fun encode(input: CharBuffer, output: ByteBuffer): CharsetTransformResult
}
