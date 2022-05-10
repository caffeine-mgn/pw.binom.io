package pw.binom.charset

import pw.binom.CharBuffer
import pw.binom.io.ByteBuffer
import pw.binom.io.Closeable

interface CharsetDecoder : Closeable {
    fun decode(input: ByteBuffer, output: CharBuffer): CharsetTransformResult
}
