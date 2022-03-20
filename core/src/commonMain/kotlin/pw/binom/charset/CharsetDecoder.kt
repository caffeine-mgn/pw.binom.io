package pw.binom.charset

import pw.binom.ByteBuffer
import pw.binom.CharBuffer

interface CharsetDecoder {
    fun decode(input: ByteBuffer, output: CharBuffer): CharsetTransformResult
}
