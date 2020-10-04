package pw.binom.io

import pw.binom.*
import pw.binom.charset.Charset
import pw.binom.charset.CharsetTransformResult
import pw.binom.charset.Charsets

class AsyncBufferedInputReader(
        charset: Charset,
        val input: AsyncInput,
        bufferSize: Int = DEFAULT_BUFFER_SIZE,
        charBufferSize: Int = DEFAULT_BUFFER_SIZE / 2
) : AsyncReader {
    private val decoder = charset.newDecoder()
    private val output = CharBuffer.alloc(charBufferSize).empty()

    private val tmp = ByteBuffer.alloc(1)
    private val buffer = ByteBuffer.alloc(bufferSize).empty()

    private suspend fun checkAvailable() {
        if (buffer.remaining == 0) {
            buffer.clear()
            input.read(buffer)
            buffer.flip()
        }
    }

    private suspend fun prepareBuffer() {
        if (output.remaining == 0) {
            checkAvailable()
            output.clear()
            if (decoder.decode(buffer, output) == CharsetTransformResult.MALFORMED) {
                throw IOException()
            }
            output.flip()
        }
    }

    override suspend fun read(data: CharArray, offset: Int, length: Int): Int {
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

    override suspend fun read(): Char? {
        prepareBuffer()
        if (output.remaining == 0) {
            return null
        }
        return output.get()
    }

    override suspend fun close() {
        decoder.close()
        tmp.close()
    }

}

private fun Input.readByte2(buffer: ByteBuffer): Byte? {
    buffer.reset(0, 1)
    if (read(buffer) != 1) {
        return null
    }
    return buffer[0]
}

fun AsyncInput.reader(charset: Charset = Charsets.UTF8, bufferSize: Int = DEFAULT_BUFFER_SIZE, charBufferSize: Int = DEFAULT_BUFFER_SIZE / 2) = AsyncBufferedInputReader(
        charset = charset,
        input = this,
        bufferSize = bufferSize,
        charBufferSize = charBufferSize
)