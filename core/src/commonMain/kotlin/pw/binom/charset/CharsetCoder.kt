package pw.binom.charset

import pw.binom.ByteBuffer
import pw.binom.CharBuffer
import pw.binom.io.Closeable
import pw.binom.io.IOException
import kotlin.math.roundToInt

class CharsetCoder(charset: Charset, charBufferCapacity: Int = 256, byteBufferCapacity: Int = 512) : Closeable {
    private val encoder = charset.newEncoder()
    private val decoder = charset.newDecoder()
    private var charBuffer = CharBuffer.alloc(charBufferCapacity)
    private var byteBuffer = ByteBuffer.alloc(byteBufferCapacity)

    fun <T> encode(text: String, using: (ByteBuffer) -> T): T {
        if (charBuffer.capacity < text.length) {
            val newCharBuffer = charBuffer.realloc(text.length)
            charBuffer.close()
            charBuffer = newCharBuffer
        }
        charBuffer.clear()
//        text.forEach {
//            charBuffer.put(it)
//        }
        charBuffer.write(text.toCharArray())
        charBuffer.flip()
        byteBuffer.clear()
        LOOP@ while (true) {
            when (val result = encoder.encode(charBuffer, byteBuffer)) {
                CharsetTransformResult.OUTPUT_OVER -> {
                    val newByteBuffer =
                        byteBuffer.realloc((byteBuffer.capacity.toFloat() * 1.7f).roundToInt() + 8)
                    byteBuffer.close()
                    byteBuffer = newByteBuffer
                    byteBuffer.limit = byteBuffer.capacity
                }
                CharsetTransformResult.INPUT_OVER, CharsetTransformResult.MALFORMED -> {
                    throw IOException("Can't decode string")
                }
                CharsetTransformResult.SUCCESS -> {
                    byteBuffer.flip()
                    return using(byteBuffer)
                }
            }
        }
    }


    fun decode(bytes: ByteArray): String {
        if (byteBuffer.capacity < bytes.size) {
            val b = byteBuffer.realloc(bytes.size)
            byteBuffer.close()
            byteBuffer = b
        }
        byteBuffer.clear()
        byteBuffer.write(bytes)
        byteBuffer.flip()
        return decode(byteBuffer)
    }

    fun decode(bytes: ByteBuffer): String {
        charBuffer.clear()
        while (true) {
            when (decoder.decode(bytes, charBuffer)) {
                CharsetTransformResult.OUTPUT_OVER -> {
                    val newCharbuffer =
                        charBuffer.realloc((charBuffer.capacity.toFloat() * 1.7f).roundToInt() + 8)
                    charBuffer.close()
                    charBuffer = newCharbuffer
                    charBuffer.limit = charBuffer.capacity
                }
                CharsetTransformResult.INPUT_OVER, CharsetTransformResult.MALFORMED -> {
                    throw IOException("Can't decode string")
                }
                CharsetTransformResult.SUCCESS -> {
                    charBuffer.flip()
                    return charBuffer.toString()
                }
            }
        }
    }

    override fun close() {
        charBuffer.close()
        byteBuffer.close()
    }
}