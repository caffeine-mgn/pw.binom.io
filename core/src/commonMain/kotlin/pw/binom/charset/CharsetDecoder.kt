package pw.binom.charset

import pw.binom.ByteBuffer
import pw.binom.CharBuffer
import pw.binom.io.Closeable

interface CharsetDecoder : Closeable {
    fun decode(input: ByteBuffer, output: CharBuffer): CharsetTransformResult
}
