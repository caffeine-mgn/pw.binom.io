package pw.binom.io

class AsyncAppendableUTF8(private val stream: AsyncOutput) : AsyncWriter {

    private val data = ByteBuffer.alloc(4)

    override suspend fun append(value: Char): AsyncAppendable {
        try {
            data.clear()
            UTF8.unicodeToUtf8(value, data)
            data.flip()
            stream.write(data)
        } catch (e: Throwable) {
            throw e
        }
        return this
    }

    override suspend fun append(value: CharSequence?): AsyncAppendable {
        value?.forEach {
            append(it)
        }
        return this
    }

    override suspend fun append(value: CharSequence?, startIndex: Int, endIndex: Int): AsyncAppendable {
        value ?: return this
        (startIndex..endIndex).forEach {
            append(value[it])
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
