package pw.binom.io

class AppendableUTF8(private val stream: Output) : Writer {
    private val data = ByteBuffer(4)
    override fun append(value: Char): AppendableUTF8 {
        data.clear()
        UTF8.unicodeToUtf8(value, data)
        data.flip()
        stream.write(data)
        return this
    }

    override fun append(value: CharSequence?): AppendableUTF8 {
        value?.forEach {
            append(it)
        }
        return this
    }

    override fun append(value: CharSequence?, startIndex: Int, endIndex: Int): AppendableUTF8 {
        value ?: return this
        (startIndex..endIndex).forEach {
            append(value[it])
        }
        return this
    }

    override fun flush() {
        stream.flush()
    }

    override fun close() {
        data.close()
        stream.close()
    }
}

fun Output.utf8Appendable() = AppendableUTF8(this)
