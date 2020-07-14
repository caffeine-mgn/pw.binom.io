package pw.binom.io.httpServer

import pw.binom.AsyncInput
import pw.binom.ByteBuffer
import pw.binom.empty
import pw.binom.io.AbstractAsyncBufferedInput
import pw.binom.io.IOException
import pw.binom.io.http.Headers
import pw.binom.io.readln
import pw.binom.io.utf8Reader
import pw.binom.pool.ObjectPool

class EndState {
    enum class Type {
        NONE, LIMIT, BLOCK_EOF, DATA_EOF
    }

    var limit: Int = 0
    var type = EndState.Type.NONE
    var skip = 0

    override fun toString(): String {
        return "EndState(limit=$limit, type=$type, skip=$skip)"
    }


}

internal const val CR = '\r'.toByte()
internal const val LF = '\n'.toByte()
internal const val MINUS = '-'.toByte()

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
                if (c.toByte() == b) {
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
                } else
                    reset()
            }

            state == separator.length + 4 + 1 && !blockEnd -> {
                if (b == MINUS) {
                    state++
                } else
                    reset()
            }

            state == separator.length + 4 + 2 && !blockEnd -> {
                if (b == CR) {
                    state++
                } else
                    reset()
            }

            state == separator.length + 4 + 3 && !blockEnd -> {
                if (b == LF) {
                    endState.type = EndState.Type.DATA_EOF
                    endState.skip = state + 1
                    break@LOOP
                } else
                    reset()
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

fun HttpRequest.multipart(bufferPool: ObjectPool<ByteBuffer>): AsyncMultipartInput? {
    val contentType = headers[Headers.CONTENT_TYPE]?.singleOrNull() ?: return null
    if (contentType.startsWith("multipart/form-data;") != true)
        return null

    val boundary = contentType.substring(contentType.indexOf(';') + 1).trim().removePrefix("boundary=")
    return AsyncMultipartInput(
            separator = boundary,
            bufferPool = bufferPool,
            stream = input
    )
}

open class AsyncMultipartInput(val separator: String, override val stream: AsyncInput, private val bufferPool: ObjectPool<ByteBuffer>) : AbstractAsyncBufferedInput() {
    override val buffer = bufferPool.borrow().empty()

    init {
        require(buffer.capacity > 2 + 2 + separator.length + 2 + 2) { "BufferSize must be grate than separator.length + 8" }
    }

    private val endState = EndState()
    private var first = true
    private val reader = utf8Reader()
    var formName: String? = null
        private set
    private val _headers = HashMap<String, ArrayList<String>>()
    val headers: Map<String, List<String>>
        get() = _headers
//    private var nextBlockReady = false

    suspend open fun next(): Boolean {
        if (first) {
            fill()
            first = false
            val firstLine = reader.readln()
            if (firstLine != "--$separator") {
                throw IOException("Invalid Input Multipart data: Invalid first data line [$firstLine]")
            }
        } else {
            formName = null
            _headers.clear()
        }

        if (endState.type == EndState.Type.DATA_EOF && buffer.remaining == 0)
            return false
        if (buffer.remaining == 0) {
            fill()
        }
        if (endState.type == EndState.Type.DATA_EOF && buffer.remaining == 0)
            return false

        val head = reader.readln() ?: throw IOException("Can't read part data header")
        val items = head.split(':', limit = 2)
        if (items[0].toLowerCase() != "content-disposition")
            throw IOException("Invalid part header \"${items[0]}\"")
        val headValues = items[1].split(';').map { it.trim() }
        if ("form-data" != headValues[0])
            throw IOException("Invalid part header: is not form-data part")
        if (!headValues[1].startsWith("name="))
            throw IOException("Invalid part header: Can't get name of part")
        formName = headValues[1].removePrefix("name=\"").removeSuffix("\"")
        _headers.clear()
        while (true) {
            val l = reader.readln() ?: ""
            if (l.isEmpty())
                break
            val headItems = l.split(':', limit = 2)
            _headers.getOrPut(headItems[0].trim()) { ArrayList() }.add(headItems[1].trim())
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
                    } else
                        buffer.clear()
                }
            } else {
                buffer.clear()
            }

            super.fill()
            if (findEnd(separator, buffer, endState)) {
                buffer.limit = endState.limit
            }
        } while (false)
    }

    init {
        endState.type = EndState.Type.BLOCK_EOF
    }

    override suspend fun read(dest: ByteBuffer): Int {
        if (buffer.remaining == 0 && (endState.type == EndState.Type.BLOCK_EOF || endState.type == EndState.Type.DATA_EOF)) {
            return 0
        }
        return super.read(dest)
    }

    override suspend fun close() {
        try {
            super.close()
        } finally {
            bufferPool.recycle(buffer)
        }
    }
}