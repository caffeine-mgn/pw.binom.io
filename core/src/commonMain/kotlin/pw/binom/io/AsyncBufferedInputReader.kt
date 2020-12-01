package pw.binom.io

import pw.binom.*
import pw.binom.charset.Charset
import pw.binom.charset.CharsetTransformResult
import pw.binom.charset.Charsets
import pw.binom.pool.ObjectPool

class AsyncBufferedInputReader(
    charset: Charset,
    val input: AsyncInput,
    private val pool: ObjectPool<ByteBuffer>,
    charBufferSize: Int = 512
) : AsyncReader {
    private val decoder = charset.newDecoder()
    private val output = CharBuffer.alloc(charBufferSize).empty()

    private val buffer = pool.borrow().empty()

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

    suspend fun readUntil(char: Char): String? {
        val out = StringBuilder()
        var exist = false
        LOOP@ while (true) {
            prepareBuffer()
            if (output.remaining <= 0) {
                break
            }
            for (i in output.position until output.limit) {
                if (output[i] == char) {
                    out.append(output.subString(output.position, i))
                    output.position = i + 1
                    exist = true
                    break@LOOP
                }
            }
            out.append(output.subString(output.position, output.limit))
            output.position = output.limit
            exist = true
        }
        if (!exist) {
            return null
        }
        return out.toString()
    }

    override suspend fun readln(): String? = readUntil(10.toChar())

    override suspend fun read(): Char? {
        prepareBuffer()
        if (output.remaining == 0) {
            return null
        }
        return output.get()
    }

    override suspend fun asyncClose() {
        decoder.close()
        pool.recycle(buffer)
    }

}

fun AsyncInput.bufferedReader(
    pool: ObjectPool<ByteBuffer>,
    charset: Charset = Charsets.UTF8,
    charBufferSize: Int = 512
) = AsyncBufferedInputReader(
    charset = charset,
    input = this,
    pool = pool,
    charBufferSize = charBufferSize
)