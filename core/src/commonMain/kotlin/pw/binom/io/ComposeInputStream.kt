package pw.binom.io

import pw.binom.PopResult
import pw.binom.Stack

class ComposeInputStream : InputStream {

    private val readers = Stack<InputStream>()
    private var current = PopResult<InputStream>()

    override fun read(data: ByteArray, offset: Int, length: Int): Int {
        while (true) {
            if (current.isEmpty && readers.isEmpty)
                return 0

            if (current.isEmpty) {
                readers.popFirst(current)
                continue
            }

            val r = current.value.read(data, offset, length)
            if (r == 0) {
                current.clear()
                continue
            }
            return r
        }
    }

    override fun close() {
        do {
            if (!current.isEmpty)
                current.value.close()

            readers.popFirst(current)
        } while (!current.isEmpty)
    }

    fun addFirst(reader: InputStream): ComposeInputStream {
        if (!current.isEmpty) {
            readers.pushFirst(current.value)
            current.clear()
        }
        readers.pushFirst(reader)
        return this
    }

    fun addLast(reader: InputStream): ComposeInputStream {
        readers.pushLast(reader)
        return this
    }
}

operator fun InputStream.plus(other: InputStream): ComposeInputStream {
    val s = ComposeInputStream()
    s.addFirst(other)
    s.addFirst(this)
    return s
}