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
    private val charBuffer = CharBuffer.alloc(charBufferSize).empty()
    private var eof = false

    init {
        buffer.empty()
    }

    private suspend fun full(): Boolean {
        if (eof) {
            return false
        }
        if (buffer.remaining > 0) {
            buffer.compact()
        } else {
            buffer.clear()
        }
        val r = input.read(buffer)
        if (r == 0) {
            eof = true
        }
        buffer.flip()
        return !eof
    }

    private suspend fun prepareBuffer() {
        if (charBuffer.remaining == 0) {
            full()
            if (buffer.remaining == 0) {
                return
            }
            charBuffer.clear()
            if (decoder.decode(buffer, charBuffer) == CharsetTransformResult.MALFORMED) {
                throw IOException("Input string is malformed")
            }
            charBuffer.flip()
        }
    }

    override suspend fun read(dest: CharArray, offset: Int, length: Int): Int {
        if (length == 0) {
            return 0
        }
        var counter = 0
        while (counter < length) {
            prepareBuffer()
            if (charBuffer.remaining > 0) {
                counter += charBuffer.read(
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

    suspend fun readUntil(func: (Char, StringBuilder) -> Boolean): String? {
        val out = StringBuilder()
        var exist = false
        LOOP@ while (true) {
            prepareBuffer()
            if (charBuffer.remaining <= 0) {
                break
            }
            for (i in charBuffer.position until charBuffer.limit) {
                if (!func(charBuffer[i], out)) {
                    val str = charBuffer.subString(charBuffer.position, i)
                    out.append(str)
                    charBuffer.position = i + 1
                    exist = true
                    break@LOOP
                }
            }
            val str = charBuffer.subString(charBuffer.position, charBuffer.limit)
            out.append(str)
            charBuffer.position = charBuffer.limit
            exist = true
        }
        if (!exist) {
            return null
        }
        return out.toString()
    }

    override suspend fun readln(): String? = readUntil({ char, _ -> char != '\n' })?.removeSuffix("\r")

    override suspend fun readChar(): Char? {
        prepareBuffer()
        if (charBuffer.remaining == 0) {
            return null
        }
        return charBuffer.get()
    }

    override suspend fun asyncClose() {
        try {
            if (closeParent) {
                input.asyncClose()
            }
        } finally {
            charBuffer.close()
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
