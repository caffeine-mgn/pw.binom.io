package pw.binom.io

import pw.binom.PopResult
import pw.binom.Stack

class ComposeReader : AbstractReader() {
    private val readers = Stack<Reader>()
    private var current = PopResult<Reader>()

    override fun read(): Char {
        while (true) {
            if (current.isEmpty) {
                readers.popFirst(current)
                if (current.isEmpty)
                    throw EOFException()
            }
            try {
                return current.value.read()
            } catch (e: EOFException) {
                current.clear()
            }
        }
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