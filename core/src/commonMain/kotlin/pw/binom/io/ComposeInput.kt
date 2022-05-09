package pw.binom.io

import pw.binom.ByteBuffer
import pw.binom.Input
import pw.binom.PopResult
import pw.binom.Stack

class ComposeInput : Input {

    private val readers = Stack<Input>()
    private var current = PopResult<Input>()

    override fun read(dest: ByteBuffer): Int {
        while (true) {
            if (current.isEmpty && readers.isEmpty)
                return 0

            if (current.isEmpty) {
                readers.popFirst(current)
                continue
            }

            val r = current.value.read(dest)
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

    fun addFirst(reader: Input): ComposeInput {
        if (!current.isEmpty) {
            readers.pushFirst(current.value)
            current.clear()
        }
        readers.pushFirst(reader)
        return this
    }

    fun addLast(reader: Input): ComposeInput {
        readers.pushLast(reader)
        return this
    }
}

operator fun Input.plus(other: Input): ComposeInput {
    if (this is ComposeInput) {
        addLast(other)
        return this
    }
    val composeInput = ComposeInput()
    composeInput.addFirst(other)
    composeInput.addFirst(this)
    return composeInput
}
