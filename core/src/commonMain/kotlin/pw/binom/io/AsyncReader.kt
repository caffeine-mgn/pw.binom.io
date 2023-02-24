package pw.binom.io

interface AsyncReader : AsyncCloseable {
    /**
     * @throws EOFException throws when stream is done
     */
    suspend fun readChar(): Char?

    suspend fun read(dest: CharArray, offset: Int = 0, length: Int = dest.size - offset): Int
    suspend fun readln(): String? {
        val sb = StringBuilder()
        try {
            while (true) {
                val r = readChar() ?: break
                if (r == 10.toChar()) {
                    break
                }
                sb.append(r)
            }
        } catch (e: EOFException) {
            // NOP
        }
        if (sb.isEmpty()) {
            return null
        }
        if (sb.lastOrNull() == '\r') {
            sb.deleteAt(sb.lastIndex)
        }
        return sb.toString()
    }

    suspend fun readText(): String {
        val sb = StringBuilder()
        while (true) {
            try {
                sb.append(readChar() ?: break)
            } catch (e: EOFException) {
                break
            }
        }
        return sb.toString()
    }
}

abstract class AbstractAsyncReader : AsyncReader {
    override suspend fun read(dest: CharArray, offset: Int, length: Int): Int {
        if (offset + length > dest.size) {
            throw IndexOutOfBoundsException()
        }
        var i = 0
        while (i < length) {
            try {
                dest[offset + i++] = readChar() ?: break
            } catch (e: EOFException) {
                return i
            }
        }
        return i
    }
}

suspend fun AsyncReader.readText(): String {
    val sb = StringBuilder()
    while (true) {
        try {
            sb.append(readChar() ?: break)
        } catch (e: EOFException) {
            break
        }
    }
    return sb.toString()
}
