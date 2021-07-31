package pw.binom.io.examples.file

import pw.binom.*
import pw.binom.crypto.MD5MessageDigest
import pw.binom.io.*
import pw.binom.io.file.File
import pw.binom.io.file.read
import pw.binom.io.file.openWrite

/**
 * Example: using ByteArray
 */
fun usingByteArray() {
    val bufferPool = ByteBufferPool(10)
    val file = File("Simple File")
    try {
        val data = "Simple Text".encodeToByteArray()
        file.openWrite().use {
            it.writeBytes(bufferPool, data)
            it.flush()
        }
        println("Write data: \"${data.decodeToString()}\"")

        val out = ByteArrayOutput()
        file.read().use {
            it.copyTo(out, bufferPool)
        }
        out.trimToSize()
        out.data.clear()
        println("Read data: \"${out.data.toByteArray().decodeToString()}\"")
        out.close()
    } finally {
        bufferPool.close()
        file.delete()
    }

}

/**
 * Example: using appender and reader
 */
fun usingAppenderAndReader() {
    val bufferPool = ByteBufferPool(10)
    val file = File("Simple File")
    try {
        val text = "Simple Text"
        file.openWrite().bufferedWriter(bufferPool).use {
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

/**
 * Example: read using byte buffer
 */
fun usingByteBuffer() {
    val bufferPool = ByteBufferPool(10)
    val file = File("Simple File")
    val text = "Simple Text"
    try {
        text.toByteBufferUTF8().use { data ->
            file.openWrite().use {
                it.write(data)
            }
        }
        println("Write data: \"$text\"")
        ByteBuffer.alloc(512).use { readBuf ->

            val read = file.read().use {
                it.read(readBuf)
            }
            readBuf.flip()

            println("Read data: \"${readBuf.toByteArray().decodeToString()}\"")
        }
    } finally {
        bufferPool.close()
        file.delete()
    }
}

/**
 * Example: calc md5
 */
fun calcMd5() {
    val file = File("Simple File")
    val text = "Simple Text"
    val md5 = MD5MessageDigest()
    try {
        text.toByteBufferUTF8().use { data ->
            file.openWrite().use {
                it.write(data)
            }
        }

        ByteBuffer.alloc(512).use { readBuf ->
            file.read().use {
                while (true) {
                    readBuf.clear()
                    if (it.read(readBuf) <= 0) {
                        break
                    }
                    readBuf.flip()
                    md5.update(readBuf)
                }
            }
        }
        val hash = md5.finish()//result md5 hash
        val hashString = hash.map {
            val int = it.toInt() and 0xFF
            (int ushr 4).toString(16) + (int and 0xF).toString(16)
        }.joinToString("")

        println("Hash: $hashString")
    } finally {
        file.delete()
    }
}

fun main(args: Array<String>) {
    usingByteArray()
    usingAppenderAndReader()
    usingByteBuffer()
    calcMd5()
}