package pw.binom.compression.zlib

import pw.binom.ByteBuffer
import pw.binom.ByteDataBuffer
import pw.binom.io.Closeable
import pw.binom.update
import java.util.zip.Inflater as JInflater

actual class Inflater actual constructor(wrap: Boolean) : Closeable {
    private val native = JInflater(!wrap)

    override fun close() {
        native.end()
    }

    actual fun inflate(cursor: Cursor, input: ByteArray, output: ByteArray): Int {
        native.setInput(input, cursor.inputOffset, cursor.availIn)
        val readed = native.bytesRead
        val writed = native.bytesWritten
        native.inflate(output, cursor.outputOffset, cursor.availOut)
        cursor.inputOffset += (native.bytesRead - readed).toInt()
        cursor.outputOffset += (native.bytesWritten - writed).toInt()
        return (native.bytesWritten - writed).toInt()
    }

    actual fun end() {
        native.end()
    }

    actual fun inflate(cursor: Cursor, input: ByteDataBuffer, output: ByteDataBuffer): Int {
        return input.update(cursor.inputOffset, cursor.availIn) { input ->
            output.update(cursor.outputOffset, cursor.availOut) { output ->
                native.setInput(input)
                val readed = native.bytesRead
                val writed = native.bytesWritten


//                c.position(cursor.outputOffset)
//                c.limit(cursor.outputOffset + cursor.availOut)

                val uncompressed = native.inflate(output)
                cursor.inputOffset += (native.bytesRead - readed).toInt()
                cursor.outputOffset += (native.bytesWritten - writed).toInt()

                uncompressed
            }
        }
    }

    actual fun inflate(input: ByteBuffer, output: ByteBuffer): Int {
        native.setInput(input.native)
        val writed = native.bytesWritten
        native.inflate(output.native)
        return (native.bytesWritten - writed).toInt()
    }

}