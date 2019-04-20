package pw.binom.io

import pw.binom.asUTF8ByteArray

interface OutputStream : Closeable {
    fun write(data: ByteArray, offset: Int=0, length: Int=data.size-offset): Int
    fun flush()
}

fun OutputStream.write(text: String) {
    val data = text.asUTF8ByteArray()
    write(data)
}