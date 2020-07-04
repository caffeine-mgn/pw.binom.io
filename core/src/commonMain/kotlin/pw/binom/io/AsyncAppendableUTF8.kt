package pw.binom.io

import pw.binom.AsyncOutput
import pw.binom.ByteDataBuffer
import pw.binom.Output
import pw.binom.tmp8

class AsyncAppendableUTF8(private val stream: AsyncOutput) : AsyncWriter {
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
        stream.flush()
        stream.close()
    }
}

fun AsyncOutput.utf8Appendable() = AsyncAppendableUTF8(this)