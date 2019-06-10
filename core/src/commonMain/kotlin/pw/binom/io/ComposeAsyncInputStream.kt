package pw.binom.io

import pw.binom.PopResult
import pw.binom.Stack

class ComposeAsyncInputStream : AsyncInputStream {

    private val readers = Stack<AsyncInputStream>()
    private var current = PopResult<AsyncInputStream>()

    override suspend fun read(data: ByteArray, offset: Int, length: Int): Int {
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

    override suspend fun close() {
        do {
            if (!current.isEmpty)
                current.value.close()

            readers.popFirst(current)
        } while (!current.isEmpty)
    }

    fun addFirst(reader: AsyncInputStream): ComposeAsyncInputStream {
        if (!current.isEmpty) {
            readers.pushFirst(current.value)
            current.clear()
        }
        readers.pushFirst(reader)
        return this
    }

    fun addLast(reader: AsyncInputStream): ComposeAsyncInputStream {
        readers.pushLast(reader)
        return this
    }
}

operator fun AsyncInputStream.plus(other: AsyncInputStream): ComposeAsyncInputStream {
    val s = ComposeAsyncInputStream()
    s.addFirst(other)
    s.addFirst(this)
    return s
}