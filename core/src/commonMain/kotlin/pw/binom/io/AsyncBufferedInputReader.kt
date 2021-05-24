package pw.binom.io

import pw.binom.*
import pw.binom.charset.Charset
import pw.binom.charset.CharsetTransformResult
import pw.binom.charset.Charsets
import pw.binom.pool.ObjectPool

class AsyncBufferedInputReader private constructor(
    charset: Charset,
    val input: AsyncInput,
    private val pool: ObjectPool<ByteBuffer>?,
    private val buffer: ByteBuffer,
    private var closeBuffer: Boolean,
    private val closeParent: Boolean,
    charBufferSize: Int = 512
) : AsyncReader {
    constructor(
        charset: Charset,
        input: AsyncInput,
        pool: ObjectPool<ByteBuffer>,
        charBufferSize: Int = 512,
        closeParent: Boolean = true,
    ) : this(
        charset = charset,
        input = input,
        pool = pool,
        buffer = pool.borrow().empty(),
        charBufferSize = charBufferSize,
        closeBuffer = false,
        closeParent = closeParent,
    )

    constructor(
        charset: Charset,
        input: AsyncInput,
        buffer: ByteBuffer,
        charBufferSize: Int = 512,
        closeParent: Boolean = true,
    ) : this(
        charset = charset,
        input = input,
        pool = null,
        buffer = buffer.empty(),
        charBufferSize = charBufferSize,
        closeBuffer = false,
        closeParent = closeParent,
    )

    constructor(
        charset: Charset,
        input: AsyncInput,
        bufferSize: Int = DEFAULT_BUFFER_SIZE,
        charBufferSize: Int = 512,
        closeParent: Boolean = true,
    ) : this(
        charset = charset,
        input = input,
        pool = null,
        buffer = ByteBuffer.alloc(bufferSize).empty(),
        charBufferSize = charBufferSize,
        closeBuffer = true,
        closeParent = closeParent,
    )

    private val decoder = charset.newDecoder()
    private val output = CharBuffer.alloc(charBufferSize).empty()
    private var eof = false

//    private val buffer = pool.borrow().empty()

    private suspend fun full() {
        if (eof) {
            return
        }
        if (buffer.remaining == 0) {
            buffer.clear()
            val r = input.read(buffer)
            if (r == 0) {
                eof = true
            }
            buffer.flip()
        }
    }

    private suspend fun prepareBuffer() {
        if (output.remaining == 0) {
            full()
            output.clear()
            if (decoder.decode(buffer, output) == CharsetTransformResult.MALFORMED) {
                throw IOException()
            }
            output.flip()
        }
    }

    override suspend fun read(dest: CharArray, offset: Int, length: Int): Int {
        if (length == 0) {
            return 0
        }
        var counter = 0
        while (counter < length) {
            prepareBuffer()
            if (output.remaining > 0) {
                counter += output.read(
                    array = dest,
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

    override suspend fun readln(): String? = readUntil(10.toChar())?.removeSuffix("\r")

    override suspend fun readChar(): Char? {
        prepareBuffer()
        if (output.remaining == 0) {
            return null
        }
        return output.get()
    }

    override suspend fun asyncClose() {
        try {
            if (closeParent) {
                input.asyncClose()
            }
        } finally {
            output.close()
            if (closeBuffer) {
                buffer.close()
            } else {
                pool?.recycle(buffer)
            }
        }
    }

}

fun AsyncInput.bufferedReader(
    pool: ObjectPool<ByteBuffer>,
    charset: Charset = Charsets.UTF8,
    charBufferSize: Int = 512,
    closeParent: Boolean = true,
) = AsyncBufferedInputReader(
    charset = charset,
    input = this,
    pool = pool,
    charBufferSize = charBufferSize,
    closeParent = closeParent,
)

fun AsyncInput.bufferedReader(
    bufferSize: Int = DEFAULT_BUFFER_SIZE,
    charset: Charset = Charsets.UTF8,
    charBufferSize: Int = bufferSize,
    closeParent: Boolean = true,
) = AsyncBufferedInputReader(
    charset = charset,
    input = this,
    charBufferSize = charBufferSize,
    bufferSize = bufferSize,
    closeParent = closeParent,
)

fun AsyncInput.bufferedReader(
    buffer: ByteBuffer,
    charset: Charset = Charsets.UTF8,
    charBufferSize: Int = buffer.capacity,
    closeParent: Boolean = true,
) = AsyncBufferedInputReader(
    charset = charset,
    input = this,
    charBufferSize = charBufferSize,
    buffer = buffer,
    closeParent = closeParent,
)