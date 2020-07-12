package pw.binom.io.httpServer

import pw.binom.AsyncInput
import pw.binom.ByteBuffer
import pw.binom.empty
import pw.binom.forEachIndexed
import pw.binom.io.AbstractAsyncBufferedInput
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
        println("$i -> state: [$state], char: [${b.toChar2()}], position: [${buffer.position}], limit: ${buffer.limit}")
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

open class ReaderWithSeparator(val separator: String, override val stream: AsyncInput, private val bufferPool: ObjectPool<ByteBuffer>) : AbstractAsyncBufferedInput() {
    override val buffer = bufferPool.borrow().empty()

    init {
        require(buffer.capacity > 2 + 2 + separator.length + 2 + 2)
    }

    private val endState = EndState()
//    private var nextBlockReady = false

    suspend open fun next(): Boolean {
        if (endState.type == EndState.Type.DATA_EOF && buffer.remaining == 0)
            return false
        if (buffer.remaining == 0) {
            fill()
        }
        if (endState.type == EndState.Type.DATA_EOF && buffer.remaining == 0)
            return false

        /*

        if (buffer.limit > 0) {
            nextBlockReady = true
            return true
        }
        nextBlockReady = false
        */
        return true
    }

    override suspend fun fill() {
        if (endState.type == EndState.Type.DATA_EOF) {
            return
        }
        do {
            /*
            if (endState.dataEof)
                TODO()
            if (!endState.blockEof && endState.limit > 0) {
                buffer.compact()
                buffer.position = endState.skip
            }

            if (buffer.remaining == 0 && endState.blockEof) {
                buffer.limit = buffer.capacity
                buffer.position = endState.skip
                buffer.compact()
                buffer.position = 0
                buffer.limit = buffer.capacity
            }
//            buffer.clear()
            println("Before Fill ${buffer.position} -> ${buffer.limit}")
            super.fill()
            println("After Fill ${buffer.position} -> ${buffer.limit}")
            if (findEnd(separator, buffer, endState)) {
                buffer.limit = endState.limit
                println("fill. limit: ${endState.limit}")
                if (!endState.dataEof && buffer.remaining == 0)
                    continue
                println("After Search END ${buffer.position} -> ${buffer.limit}")
            }
            */
//            if (endState.type==EndState.Type.LIMIT) {
//                buffer.compact()
//                buffer.compact()
//            }

            if (endState.type == EndState.Type.BLOCK_EOF) {
                print("Before Compact: ")
                buffer.print()
                buffer.limit = buffer.capacity
                buffer.position += endState.skip
                buffer.compact()
                buffer.position = endState.skip
                print("After Compact: ")
                buffer.print()
            } else {
                println("fill!!!! endState: [$endState]")
                buffer.clear()
            }

            super.fill()
            buffer.limit = buffer.capacity
            if (findEnd(separator, buffer, endState)) {
                buffer.limit = endState.limit
                println("After fill remaining: [${buffer.remaining}], endState: [$endState]")
                buffer.print()
            }
        } while (false)
    }

    init {
        endState.type = EndState.Type.BLOCK_EOF
    }

    override suspend fun read(dest: ByteBuffer): Int {
        /*
        if (buffer.remaining == 0 && !nextBlockReady)
            return 0
        println("----====Buffer Body====----")
        (buffer.position until buffer.limit).forEach {
            println("$it -> ${buffer[it].toChar2()}")
        }
        println("----==== ${buffer.position} -> ${buffer.limit} ====----")
        val r = super.read(dest)
        if (buffer.remaining == 0 && endState.blockEof) {
            nextBlockReady = false
            println("END BLOCK!")
        }
        println("After Read: buffer.remaining: [${buffer.remaining}], endState: [$endState]")
        return r
        */
        if (buffer.remaining == 0 && (endState.type == EndState.Type.BLOCK_EOF || endState.type == EndState.Type.DATA_EOF))
            return 0
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

/*
import pw.binom.AsyncInput
import pw.binom.ByteBuffer
import pw.binom.empty
import pw.binom.io.AbstractAsyncBufferedInput
import pw.binom.io.http.Headers
import pw.binom.io.readln
import pw.binom.io.utf8Reader




/*class ReaderWithSeparator(val separator: ByteArray, override val stream: AsyncInput) : AbstractAsyncBufferedInput() {
    override val buffer: ByteBuffer = ByteBuffer.alloc(1).empty()

    private val blockEnd = false

    override suspend fun read(dest: ByteBuffer): Int {
        buffer.clear()
        stream.read(buffer)
        buffer.clear()
        if (buffer[0] == CR) {

        } else {
            dest.write()
        }
    }

    override suspend fun close() {
    }

}*/

class MultipartReader(val input: AsyncInput, val boundary: String) : AsyncInput {

    constructor(request: HttpRequest) : this(
            input = request.input,
            boundary = (request.headers[Headers.CONTENT_TYPE]?.singleOrNull()
                    ?: throw IllegalStateException("Request does't have Content-Type with value multipart/form-data"))
                    .splitToSequence(';')
                    .filter { it.startsWith("boundary=") }
                    .map { it.removePrefix("boundary=\"").removeSuffix("\"") }
                    .firstOrNull()
                    ?: "boundary"
    )

    private val reader = this.utf8Reader()
    private var first = true
    private var eof = false
    private var blockEof = false
    private val headers = HashMap<String, ArrayList<String>>()


    suspend fun next(): Boolean {
        if (eof)
            return false
        needNext = false
        blockEof = false
        state = 0
        if (first) {
            while (true) {
                val s = reader.readln()
                if (s == null) {
                    eof = true
                    return false
                }
                if (s == "--$boundary") {
                    break
                }
                if (s == "--$boundary--") {
                    eof = true
                    return false
                }
            }
            first = false
        }
        headers.clear()
        while (true) {
            val s = reader.readln()
            println("Block Header: [$s]")
            if (s == null || s.isEmpty())
                break
            val items = s.split(": ", limit = 2)
            headers.getOrPut(items[0]) { ArrayList() }.add(items[1])
        }
        println("Part Header Readed!")
        if (headers.isEmpty()) {
            eof = true
            return false
        }
        return true
    }

    private var state = 0
    private var blockLimit = 0
    private var needNext = true
    private var stateReadingEndOfBlock = false

    private fun Byte.toChar2() = when (this) {
        '\r'.toByte() -> "\\r"
        '\n'.toByte() -> "\\n"
        '\t'.toByte() -> "\\t"
        else -> this.toChar()
    }

    private fun checkBlockEnd() {
        val startState = state
        blockLimit = buffer.limit
        var lastPos = 0
        LOOP@ for (it in buffer.position until buffer.limit) {
            val b = buffer[it]
            lastPos = it
            println("$it -> b=0x${b.toString(16)} [${b.toChar2()}], state: [$state]")
            when {
                state == 0 && b == '\r'.toByte() -> state++
                state == 1 -> if (b == LF) state++ else state = 0
                state == 2 -> if (b == MINUS) state++ else state = 0
                state == 3 -> if (b == MINUS) state++ else state = 0
                state in (4 until (4 + boundary.length)) -> {
                    val c = boundary[state - 4]
                    if (c.toByte() == b)
                        state++
                    else state = 0
                }
                state - 4 == boundary.length -> {
                    if (!stateReadingEndOfBlock && b == '\r'.toByte()) {
                        state++
                        continue@LOOP
                    }

                    if (b == '-'.toByte()) {
                        stateReadingEndOfBlock = true
                        state++
                        continue@LOOP
                    }
                    state = 0
                }

                state - 4 == boundary.length + 1 -> {
                    if (!stateReadingEndOfBlock && b == '\n'.toByte()) {
                        blockEof = true
                        buffer.limit = it - (state - startState)

                        println("Block EOF! state: $state, pos: $it, limit: ${buffer.limit}")
                        break@LOOP
                    }

                    if (stateReadingEndOfBlock && b == MINUS) {
                        state++
                        continue@LOOP
                    }
                    state = 0
                }
                stateReadingEndOfBlock && state - 4 == boundary.length + 2 -> {
                    if (b == CR) {
                        state++
                        continue@LOOP
                    }
                    state = 0
                }

                stateReadingEndOfBlock && state - 4 == boundary.length + 3 -> {
                    if (stateReadingEndOfBlock && b == LF) {
                        blockEof = true
//                        buffer.limit = it - (state - startState)

                        println("END OF BODY")
                        break@LOOP
                    }
                    state = 0
                }
            }
        }
        if (state > 0) {
            buffer.limit = lastPos - (state - startState)
            println("----====SET LIMIT: [${buffer.limit}], blockEof: [$blockEof]====----")
        }
    }

    protected open suspend fun fill() {
        buffer.clear()
        input.read(buffer)
        buffer.flip()

        checkBlockEnd()

    }

    override suspend fun read(dest: ByteBuffer): Int {
        if (needNext)
            return 0
        if (buffer.remaining == 0) {
            if (blockEof)
                return 0
            fill()
        }
        val l = buffer.read(dest)
        if (blockEof && buffer.remaining == 0) {
            needNext = true
            println("You read last part of previusly block")
            if (blockLimit > buffer.limit + 6 + boundary.length) {
                buffer.limit = blockLimit
                buffer.position += 6 + boundary.length
                state = 0
                stateReadingEndOfBlock = false
                checkBlockEnd()
            }
        }
        return l
    }

    override suspend fun close() {
        TODO("Not yet implemented")
    }

    private val buffer: ByteBuffer = ByteBuffer.alloc(3).empty()
}
 */

fun Byte.toChar2() = when (this) {
    '\r'.toByte() -> "\\r"
    '\n'.toByte() -> "\\n"
    '\t'.toByte() -> "\\t"
    0.toByte() -> "--"
    else -> " " + this.toChar()
}

fun ByteBuffer.print() {
    val p = position
    val l = limit

    clear()
    forEachIndexed { i, byte ->
        if (i > 0)
            print(", ")
        when {
            i == p && i == l -> print("[] ${byte.toChar2()}")
            i == p -> print("[${byte.toChar2()}")
            i == l -> print("]${byte.toChar2()}")
            else -> print("${byte.toChar2()}")
        }
    }
    if (l == capacity)
        print("]")
    println()
    limit = l
    position = p
}