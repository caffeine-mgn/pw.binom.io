package pw.binom.io.http

import pw.binom.NullAsyncOutput
import pw.binom.charset.Charset
import pw.binom.charset.Charsets
import pw.binom.copyTo
import pw.binom.io.*
import pw.binom.pool.ObjectPool
import pw.binom.skipAll

class EndState {
    enum class Type {
        NONE, LIMIT, BLOCK_EOF, DATA_EOF
    }

    var limit: Int = 0
    var type = Type.NONE
    var skip = 0

    override fun toString(): String {
        return "EndState(limit=$limit, type=$type, skip=$skip)"
    }
}

internal const val MINUS = '-'.code.toByte()

internal fun findEnd(separator: String, buffer: ByteBuffer, endState: EndState): Boolean {
    var state = 0
    var start = 0
    var blockEnd = false
    fun reset() {
        state = 0
        start = 0
    }
    endState.type = EndState.Type.NONE
    LOOP@ for (i in buffer.position until buffer.limit) {
        val b = buffer[i]
        when {
            state == 0 && b == CR -> {
                state++
                start = i
            }

            state == 1 -> if (b == LF) state++ else reset()
            state == 2 -> if (b == MINUS) state++ else reset()
            state == 3 -> if (b == MINUS) state++ else reset()
            state >= 4 && state - 4 < separator.length -> {
                val c = separator[state - 4]
                if (c.code.toByte() == b) {
                    state++
                } else {
                    reset()
                }
            }

            state == separator.length + 4 -> {
                when (b) {
                    CR -> {
                        blockEnd = true
                        state++
                    }

                    MINUS -> {
                        blockEnd = false
                        state++
                    }

                    else -> reset()
                }
            }

            state == separator.length + 4 + 1 && blockEnd -> {
                if (b == LF) {
                    endState.type = EndState.Type.BLOCK_EOF
                    endState.skip = state + 1
                    break@LOOP
                } else {
                    reset()
                }
            }

            state == separator.length + 4 + 1 && !blockEnd -> {
                if (b == MINUS) {
                    state++
                } else {
                    reset()
                }
            }

            state == separator.length + 4 + 2 && !blockEnd -> {
                if (b == CR) {
                    state++
                } else {
                    reset()
                }
            }

            state == separator.length + 4 + 3 && !blockEnd -> {
                if (b == LF) {
                    endState.type = EndState.Type.DATA_EOF
                    endState.skip = state + 1
                    break@LOOP
                } else {
                    reset()
                }
            }
        }
    }
    return if (state > 0) {
        if (endState.type == EndState.Type.NONE) {
            endState.type = EndState.Type.LIMIT
        }
        endState.limit = start
        true
    } else {
        false
    }
}

open class AsyncMultipartInput(
    private val separator: String,
    override val stream: AsyncInput,
    private val bufferPool: ObjectPool<ByteBuffer>
) : AbstractAsyncBufferedInput() {
    override val buffer = bufferPool.borrow().empty()

    init {
        require(buffer.capacity > 2 + 2 + separator.length + 2 + 2) { "BufferSize must be grate than separator.length + 8" }
    }

    private val endState = EndState()
    private var first = true
    private val reader = utf8Reader()
    var formName: String? = null
        private set
    private val _headers = HashHeaders()
    val headers: Headers
        get() = _headers
//    private var nextBlockReady = false

    open suspend fun next(): Boolean {
        if (first) {
            fill()
            first = false
            val firstLine = reader.readln()
            if (firstLine != "--$separator") {
                throw IOException("Invalid Input Multipart data: Invalid first data line [$firstLine]")
            }
        } else {
            if (!isBlockEof) {
                val buf = bufferPool.borrow()
                try {
                    skipAll(buf)
                } finally {
                    bufferPool.recycle(buf)
                }
            }
            formName = null
            _headers.clear()
        }

        if (endState.type == EndState.Type.DATA_EOF && buffer.remaining == 0) {
            return false
        }
        if (buffer.remaining == 0) {
            fill()
        }
        if (endState.type == EndState.Type.DATA_EOF && buffer.remaining == 0) {
            return false
        }

        val head = reader.readln() ?: throw IOException("Can't read part data header")
        val items = head.split(':', limit = 2)
        if (items[0].lowercase() != "content-disposition") {
            throw IOException("Invalid part header \"${items[0]}\"")
        }
        val headValues = items[1].split(';').map { it.trim() }
        if ("form-data" != headValues[0]) {
            throw IOException("Invalid part header: is not form-data part")
        }
        if (!headValues[1].startsWith("name=")) {
            throw IOException("Invalid part header: Can't get name of part")
        }
        formName = headValues[1].removePrefix("name=\"").removeSuffix("\"")
        _headers.clear()
        while (true) {
            val l = reader.readln() ?: ""
            if (l.isEmpty()) {
                break
            }
            val headItems = l.split(':', limit = 2)
            _headers.add(
                key = headItems[0].trim(),
                value = headItems[1].trim()
            )
        }
        return true
    }

    override suspend fun fill() {
        if (endState.type == EndState.Type.DATA_EOF) {
            return
        }
        do {
            if (!first) {
                if (endState.type == EndState.Type.BLOCK_EOF) {
                    buffer.limit = buffer.capacity
                    buffer.position += endState.skip
                    buffer.compact()
                } else {
                    if (endState.type == EndState.Type.LIMIT) {
                        buffer.limit = buffer.capacity
                        buffer.compact()
                    } else {
                        buffer.clear()
                    }
                }
            } else {
                buffer.clear()
            }

            try {
                super.fill()
                if (findEnd(separator, buffer, endState)) {
                    buffer.limit = endState.limit
                }
            } catch (e: Throwable) {
                buffer.empty()
                throw e
            }
        } while (false)
    }

    init {
        endState.type = EndState.Type.BLOCK_EOF
    }

    val isBlockEof
        get() = buffer.remaining == 0 && (endState.type == EndState.Type.BLOCK_EOF || endState.type == EndState.Type.DATA_EOF)

    suspend fun readText(charset: Charset = Charsets.UTF8) =
        bufferedReader(closeParent = false, charset = charset).use { it.readText() }

    suspend fun readBinary(): ByteArray {
        ByteArrayOutput().use { out ->
            copyTo(out)
            return out.toByteArray()
        }
    }

    override suspend fun read(dest: ByteBuffer): Int {
        if (isBlockEof) {
            return 0
        }
        return super.read(dest)
    }

    override suspend fun asyncClose() {
        if (closed) {
            return
        }
        try {
            if (!isBlockEof) {
                copyTo(NullAsyncOutput)
            }
            while (next()) {
                copyTo(NullAsyncOutput)
            }
        } finally {
            super.asyncClose()
            bufferPool.recycle(buffer)
        }
    }
}
