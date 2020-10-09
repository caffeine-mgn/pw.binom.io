package pw.binom.io

import pw.binom.*
import pw.binom.charset.Charset
import pw.binom.charset.CharsetTransformResult
import pw.binom.charset.Charsets

class BufferedInputReader(charset: Charset, val input: Input, bufferSize: Int = DEFAULT_BUFFER_SIZE, charBufferSize: Int = DEFAULT_BUFFER_SIZE / 2) : Reader {
    private val decoder = charset.newDecoder()
    private val output = CharBuffer.alloc(charBufferSize).empty()

    private val buffer = ByteBuffer.alloc(bufferSize).empty()

    private fun checkAvailable() {
        buffer.compact()
        input.read(buffer)
        buffer.flip()
    }

    private fun prepareBuffer() {
        if (output.remaining == 0) {
            checkAvailable()
            output.clear()
            val r = decoder.decode(buffer, output)
            if (r == CharsetTransformResult.MALFORMED) {
                throw IOException()
            }
            output.flip()
        }
    }

    override fun read(data: CharArray, offset: Int, length: Int): Int {
        if (length == 0) {
            return 0
        }
        var counter = 0
        while (counter < length) {
            prepareBuffer()
            if (output.remaining > 0) {
                counter += output.read(
                        array = data,
                        offset = offset,
                        length = length
                )
            } else {
                break
            }
        }
        return counter
    }

    override fun read(): Char? {
        prepareBuffer()
        if (output.remaining == 0) {
            return null
        }
        return output.get()
    }

    override fun close() {
        decoder.close()
    }

}

private fun Input.readByte2(buffer: ByteBuffer): Byte? {
    buffer.reset(0, 1)
    if (read(buffer) != 1) {
        return null
    }
    return buffer[0]
}

fun Input.reader(charset: Charset = Charsets.UTF8, bufferSize: Int = DEFAULT_BUFFER_SIZE, charBufferSize: Int = DEFAULT_BUFFER_SIZE / 2) = BufferedInputReader(
        charset = charset,
        input = this,
        bufferSize = bufferSize,
        charBufferSize = charBufferSize
)