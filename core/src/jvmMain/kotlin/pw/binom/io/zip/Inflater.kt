package pw.binom.io.zip

import pw.binom.io.Closeable
import java.util.zip.Inflater as JInflater

actual class Inflater actual constructor(wrap: Boolean) : Closeable {
    private val native = JInflater(!wrap)

    override fun close() {
        native.end()
    }

    actual constructor() : this(true)

    actual fun inflate(cursor: Cursor, input: ByteArray, output: ByteArray) {
        native.setInput(input, cursor.inputOffset, cursor.availIn)
        val readed = native.bytesRead
        val writed = native.bytesWritten
        native.inflate(output, cursor.outputOffset, cursor.availOut)
        cursor.inputOffset += (native.bytesRead - readed).toInt()
        cursor.outputOffset += (native.bytesWritten - writed).toInt()
    }

}