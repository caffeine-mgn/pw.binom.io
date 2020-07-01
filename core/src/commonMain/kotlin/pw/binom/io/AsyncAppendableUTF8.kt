package pw.binom.io

import pw.binom.AsyncOutput
import pw.binom.ByteDataBuffer
import pw.binom.Output
import pw.binom.tmp8

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
    override suspend fun append(c: Char): AsyncAppendable {
        try {
            tmp8.clear()
            UTF8.unicodeToUtf8(c, tmp8)
            tmp8.flip()
            stream.write(tmp8)
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