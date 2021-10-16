package pw.binom.io

import pw.binom.*
import pw.binom.charset.Charset
import pw.binom.charset.CharsetTransformResult
import pw.binom.charset.Charsets
import pw.binom.pool.ObjectPool

class BufferedInputReader(
    charset: Charset,
    val input: Input,
    private val pool: ObjectPool<ByteBuffer>?,
    private val buffer: ByteBuffer,
    private var closeBuffer: Boolean,
    private val closeParent: Boolean,
    charBufferSize: Int = 512
//    charset: Charset,
//    val input: Input,
//    private val pool: ByteBufferPool,
//    charBufferSize: Int = 512
) : Reader {

    constructor(
        charset: Charset,
        input: Input,
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
        input: Input,
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
        input: Input,
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
                throw IOException("Input string is malformed")
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
        try {
            if (closeParent) {
                input.close()
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

private fun Input.readByte2(buffer: ByteBuffer): Byte? {
    buffer.reset(0, 1)
    if (read(buffer) != 1) {
        return null
    }
    return buffer[0]
}

fun Input.bufferedReader(
    pool: ByteBufferPool,
    charset: Charset = Charsets.UTF8,
    charBufferSize: Int = 512
) = BufferedInputReader(
    charset = charset,
    input = this,
    pool = pool,
    charBufferSize = charBufferSize
)
fun Input.bufferedReader(
    bufferSize: Int = DEFAULT_BUFFER_SIZE,
    charset: Charset = Charsets.UTF8,
    charBufferSize: Int = bufferSize,
    closeParent: Boolean = true,
) = BufferedInputReader(
    charset = charset,
    input = this,
    charBufferSize = charBufferSize,
    bufferSize = bufferSize,
    closeParent = closeParent,
)