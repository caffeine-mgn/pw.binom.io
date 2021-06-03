package pw.binom.charset

import pw.binom.ByteBuffer
import pw.binom.CharBuffer
import pw.binom.IntDataBuffer
import pw.binom.io.Closeable

interface CharsetDecoder {
    fun decode(input: ByteBuffer, output: CharBuffer): CharsetTransformResult
}