package pw.binom.io

import pw.binom.AsyncOutput
import pw.binom.ByteDataBuffer
import pw.binom.Output

class AsyncAppendableUTF8(private val stream: AsyncOutputStream) : AsyncAppendable {
    private val data = ByteArray(6)
    override suspend fun append(c: Char): AsyncAppendable {
        stream.write(data, 0, UTF8.unicodeToUtf8(c, data))
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
}

class AsyncAppendableUTF82(private val stream: AsyncOutput) : AsyncWriter {
    private val data = ByteDataBuffer.alloc(4)
    override suspend fun append(c: Char): AsyncAppendable {
        try {
            val r=UTF8.unicodeToUtf8(c, data)
            stream.write(data, 0, r)
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

    override suspend fun close() {
        stream.close()
    }
}

fun AsyncOutputStream.utf8Appendable() = AsyncAppendableUTF8(this)
fun AsyncOutput.utf8Appendable() = AsyncAppendableUTF82(this)