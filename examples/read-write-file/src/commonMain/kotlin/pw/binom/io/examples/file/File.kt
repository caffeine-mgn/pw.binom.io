package pw.binom.io.examples.file

import pw.binom.*
import pw.binom.io.*
import pw.binom.io.file.File
import pw.binom.io.file.read
import pw.binom.io.file.write

fun usingByteArray() {
    val bufferPool = ByteBufferPool(10)
    val file = File("Simple File")
    try {
        val data = "Simple Text".encodeBytes()
        file.write().use {
            it.writeBytes(bufferPool, data)
            it.flush()
        }
        println("Write data: \"${data.decodeString()}\"")

        val out = ByteArrayOutput()
        file.read().use {
            it.copyTo(out, bufferPool)
        }
        out.trimToSize()
        out.data.clear()
        println("Read data: \"${out.data.toByteArray().decodeString()}\"")
        out.close()
    } finally {
        bufferPool.close()
        file.delete()
    }

}

fun usingAppenderAndReader() {
    val bufferPool = ByteBufferPool(10)
    val file = File("Simple File")
    try {
        val text = "Simple Text"
        file.write().bufferedAppendable(bufferPool).use {
            it.append(text)
            it.flush()
        }
        println("Write data: \"$text\"")

        val read = file.read().bufferedReader(bufferPool).use {
            it.readText()
        }

        println("Read data: \"${read}\"")
    } finally {
        bufferPool.close()
        file.delete()
    }
}

fun usingByteBuffer() {
    val bufferPool = ByteBufferPool(10)
    val file = File("Simple File")
    val text = "Simple Text"
    try {
        text.toByteBufferUTF8().use { data ->
            file.write().use {
                it.write(data)
            }
        }
        println("Write data: \"$text\"")
        ByteBuffer.alloc(512).use { readBuf ->

            val read = file.read().use {
                it.read(readBuf)
            }
            readBuf.flip()

            println("Read data: \"${readBuf.toByteArray().decodeString()}\"")
        }
    } finally {
        bufferPool.close()
        file.delete()
    }
}

fun main(args: Array<String>) {
    usingByteArray()
    usingAppenderAndReader()
}