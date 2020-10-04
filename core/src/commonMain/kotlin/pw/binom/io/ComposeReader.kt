package pw.binom.io

import pw.binom.PopResult
import pw.binom.Stack

class ComposeReader : Reader {
    private val readers = Stack<Reader>()
    private var current = PopResult<Reader>()

    override fun read(): Char? {
        while (true) {
            if (current.isEmpty) {
                readers.popFirst(current)
                if (current.isEmpty)
                    return null
            }
            val r = current.value.read()
            if (r == null) {
                current.clear()
                continue
            }
            return r
        }
    }

    override fun read(data: CharArray, offset: Int, length: Int): Int {
        var off = offset
        var len = length

        while (len > 0) {
            while (true) {
                if (current.isEmpty) {
                    readers.popFirst(current)
                    if (current.isEmpty)
                        return off - offset
                }
                val l = current.value.read(data, off, len)
                len -= l
                off += l
                if (l == 0)
                    current.clear()
                else
                    break
            }
        }
        return off - offset
    }

    fun addFirst(reader: Reader): ComposeReader {
        if (!current.isEmpty) {
            readers.pushFirst(current.value)
            current.clear()
        }
        readers.pushFirst(reader)
        return this
    }

    fun addLast(reader: Reader): ComposeReader {
        readers.pushLast(reader)
        return this
    }

    override fun close() {
        if (!current.isEmpty)
            current.value.close()
        while (true) {
            readers.popFirst(current)
            if (current.isEmpty)
                break
            current.value.close()
        }
    }
}

operator fun Reader.plus(other: Reader) =
        ComposeReader().addLast(this).addLast(other)