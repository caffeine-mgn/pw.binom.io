package pw.binom.io

import pw.binom.*

class AsyncAppendableUTF8(private val stream: AsyncOutput) : AsyncWriter {

    private val data = ByteBuffer.alloc(4)

    override suspend fun append(c: Char): AsyncAppendable {
        try {
            data.clear()
            UTF8.unicodeToUtf8(c, data)
            data.flip()
            stream.write(data)
        } catch (e:Throwable) {
            throw e
        }
        return this
    }

    override suspend fun append(csq: CharSequence?): AsyncAppendable {
        csq?.forEach {
            append(it)
        }
        return this
    }

    override suspend fun append(csq: CharSequence?, start: Int, end: Int): AsyncAppendable {
        csq ?: return this
        (start..end).forEach {
            append(csq[it])
        }
        return this
    }

    override suspend fun flush() {
        stream.flush()
    }

    override suspend fun asyncClose() {
        data.close()
        stream.asyncClose()
    }
}

fun AsyncOutput.utf8Appendable() = AsyncAppendableUTF8(this)