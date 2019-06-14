package pw.binom.io

import pw.binom.PopResult
import pw.binom.Stack

class ComposeAsyncReader : AbstractAsyncReader() {
    private val readers = Stack<AsyncReader>()
    private var current = PopResult<AsyncReader>()

    override suspend fun read(): Char? {
        while (true) {
            if (current.isEmpty) {
                readers.popFirst(current)
                if (current.isEmpty)
                    throw EOFException()
            }
            try {
                val r = current.value.read()
                if (r==null){
                    current.clear()
                    continue
                }
                return r
            } catch (e: EOFException) {
                current.clear()
            }
        }
    }

    fun addFirst(reader: AsyncReader): ComposeAsyncReader {
        if (!current.isEmpty) {
            readers.pushFirst(current.value)
            current.clear()
        }
        readers.pushFirst(reader)
        return this
    }

    fun addLast(reader: AsyncReader): ComposeAsyncReader {
        readers.pushLast(reader)
        return this
    }

    override suspend fun close() {
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

operator fun AsyncReader.plus(other: AsyncReader) =
        ComposeAsyncReader().addLast(this).addLast(other)