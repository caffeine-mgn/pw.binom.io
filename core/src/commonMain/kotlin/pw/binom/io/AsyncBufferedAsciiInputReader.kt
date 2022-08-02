package pw.binom.io

import pw.binom.DEFAULT_BUFFER_SIZE
import pw.binom.pool.ObjectPool

class AsyncBufferedAsciiInputReader private constructor(
    val stream: AsyncInput,
//    val bufferSize: Int = DEFAULT_BUFFER_SIZE,
    private val pool: ObjectPool<ByteBuffer>?,
    private val buffer: ByteBuffer,
    private var closeBuffer: Boolean,
    val closeParent: Boolean = true,
) : AsyncReader, AsyncInput {

    constructor(
        stream: AsyncInput,
        pool: ObjectPool<ByteBuffer>,
        closeParent: Boolean = true,
    ) : this(
        stream = stream,
        pool = pool,
        buffer = pool.borrow().empty(),
        closeBuffer = false,
        closeParent = closeParent,
    )

    constructor(
        stream: AsyncInput,
        buffer: ByteBuffer,
        closeParent: Boolean = true,
    ) : this(
        stream = stream,
        pool = null,
        buffer = buffer.empty(),
        closeBuffer = false,
        closeParent = closeParent,
    )

    constructor(
        stream: AsyncInput,
        bufferSize: Int = DEFAULT_BUFFER_SIZE,
        closeParent: Boolean = true,
    ) : this(
        stream = stream,
        pool = null,
        buffer = ByteBuffer.alloc(bufferSize).empty(),
        closeBuffer = true,
        closeParent = closeParent,
    )

    private var eof = false
    private var closed = false

    fun reset() {
        checkClosed()
        buffer.empty()
    }

    private fun checkClosed() {
        if (closed) {
            throw ClosedException()
        }
    }

//    private val buffer = ByteBuffer.alloc(bufferSize).empty()

    override val available: Int
        get() = if (closed) 0 else if (buffer.remaining > 0) buffer.remaining else -1

    private suspend fun full() {
        if (eof) {
            return
        }
        if (buffer.remaining > 0) {
            return
        }
        try {
            buffer.clear()
            val len = stream.read(buffer)
            if (len == 0) {
                eof = true
            } else {
                buffer.flip()
                val data = buffer.holdState {
                    it.toByteArray().map { it.toInt().toChar() }.joinToString("")
                }
            }
        } catch (e: Throwable) {
            buffer.empty()
            throw e
        }
    }

    override suspend fun read(dest: ByteBuffer): Int {
        checkClosed()
        full()
        return buffer.read(dest)
    }

    override suspend fun asyncClose() {
        checkClosed()
        closed = true
        if (closeBuffer) {
            buffer.close()
        } else {
            pool?.recycle(buffer)
        }
        if (closeParent) {
            stream.asyncClose()
        }
    }

    override suspend fun readChar(): Char? {
        checkClosed()
        full()
        if (buffer.remaining <= 0) {
            return null
        }
        return buffer.getByte().toInt().toChar()
    }

    override suspend fun read(dest: CharArray, offset: Int, length: Int): Int {
        checkClosed()
        full()
        val len = minOf(minOf(dest.size - offset, length), buffer.remaining)
        for (i in offset until offset + len) {
            dest[i] = buffer.getByte().toInt().toChar()
        }
        return len
    }

    suspend fun read(dest: ByteArray, offset: Int = 0, length: Int = dest.size - offset): Int {
        checkClosed()
        full()
        val len = minOf(minOf(dest.size - offset, length), buffer.remaining)
        buffer.read(
            dest = dest,
            offset = offset,
            length = len,
        )
        return len
    }

    suspend fun readFully(dest: ByteArray, offset: Int = 0, length: Int = dest.size - offset): Int {
        checkClosed()
        var readed = 0
        while (true) {
            val r = read(dest, offset + readed, length - readed)
            readed += r
            if (readed == length) {
                return length
            }
            if (r == 0) {
                throw EOFException()
            }
        }
    }

    suspend fun readUntil(char: Char): String? {
        checkClosed()
        val out = StringBuilder()
        var exist = false
        val charValue = char.code.toByte()
        LOOP@ while (true) {
            full()
            if (buffer.remaining <= 0) {
                break
            }
            val byte = buffer.getByte()
            if (charValue == byte) {
                exist = true
                break@LOOP
            }
            out.append(byte.toInt().toChar())

//            for (i in buffer.position until buffer.limit) {
//                buffer.position++
//                if (buffer[i] == char.code.toByte()) {
//                    exist = true
//                    break@LOOP
//                } else {
//                    out.append(buffer[i].toInt().toChar())
//                }
//            }
            exist = true
        }
        if (!exist) {
            return null
        }
        return out.toString()
    }

    override suspend fun readln(): String? = readUntil(10.toChar())?.removeSuffix("\r")
}

fun AsyncInput.bufferedAsciiReader(bufferSize: Int = DEFAULT_BUFFER_SIZE, closeParent: Boolean = true) =
    AsyncBufferedAsciiInputReader(
        stream = this,
        bufferSize = bufferSize,
        closeParent = closeParent,
    )

fun AsyncInput.bufferedAsciiReader(pool: ObjectPool<ByteBuffer>, closeParent: Boolean = true) =
    AsyncBufferedAsciiInputReader(
        stream = this,
        pool = pool,
        closeParent = closeParent,
    )
