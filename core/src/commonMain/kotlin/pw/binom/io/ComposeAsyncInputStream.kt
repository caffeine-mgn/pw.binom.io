package pw.binom.io

import pw.binom.PopResult
import pw.binom.Stack

class ComposeAsyncInputStream : AsyncInputStream {
    override suspend fun read(): Byte {
        if (read(staticData) != 1)
            throw EOFException()
        return staticData[0]
    }

    private val readers = Stack<AsyncInputStream>()
    private var current = PopResult<AsyncInputStream>()
    private val staticData = ByteArray(1)
    private var closed = false

    override suspend fun read(data: ByteArray, offset: Int, length: Int): Int {
        checkClose()
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
        checkClose()
        closed = true
        do {
            if (!current.isEmpty)
                current.value.close()

            readers.popFirst(current)
        } while (!current.isEmpty)
    }

    fun addFirst(reader: AsyncInputStream): ComposeAsyncInputStream {
        checkClose()
        if (!current.isEmpty) {
            readers.pushFirst(current.value)
            current.clear()
        }
        readers.pushFirst(reader)
        return this
    }

    fun addLast(reader: AsyncInputStream): ComposeAsyncInputStream {
        checkClose()
        readers.pushLast(reader)
        return this
    }

    protected fun checkClose() {
        if (closed)
            throw StreamClosedException()
    }
}

operator fun AsyncInputStream.plus(other: AsyncInputStream): ComposeAsyncInputStream {
    val s = ComposeAsyncInputStream()
    s.addFirst(other)
    s.addFirst(this)
    return s
}