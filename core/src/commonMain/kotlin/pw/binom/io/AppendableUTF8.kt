package pw.binom.io

import pw.binom.concurrency.SpinLock
import pw.binom.concurrency.synchronize

class AppendableUTF8(private val stream: Output) : Writer {
    private val data = ByteBuffer(4)
    private val spinLock = SpinLock("AppendableUTF8")
    override fun append(value: Char): AppendableUTF8 {
        spinLock.synchronize {
            data.clear()
            UTF8.unicodeToUtf8(value, data)
            data.flip()
            stream.write(data)
        }
        return this
    }

    override fun append(value: CharSequence?): AppendableUTF8 {
        spinLock.synchronize {
            value?.forEach {
                append(it)
            }
        }
        return this
    }

    override fun append(value: CharSequence?, startIndex: Int, endIndex: Int): AppendableUTF8 {
        spinLock.synchronize {
            value ?: return this
            (startIndex..endIndex).forEach {
                append(value[it])
            }
            return this
        }
    }

    override fun flush() {
        stream.flush()
    }

    override fun close() {
        spinLock.synchronize {
            data.close()
            stream.close()
        }
    }
}

fun Output.utf8Appendable() = AppendableUTF8(this)
