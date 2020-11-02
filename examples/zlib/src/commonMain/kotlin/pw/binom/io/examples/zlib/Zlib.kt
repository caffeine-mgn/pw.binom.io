package pw.binom.io.examples.zlib

import pw.binom.ByteBuffer
import pw.binom.DEFAULT_BUFFER_SIZE
import pw.binom.compression.zlib.GZIPInput
import pw.binom.compression.zlib.GZIPOutput
import pw.binom.io.file.AccessType
import pw.binom.io.file.File
import pw.binom.io.file.channel
import pw.binom.io.use
import pw.binom.toByteBufferUTF8

fun main() {
    val data = "Simple Text".toByteBufferUTF8()

    val LEVEL = 9
    val WRAP = true

    val file = File("Test.gz")

    file.channel(AccessType.WRITE, AccessType.CREATE).use {
        GZIPOutput(it, LEVEL, DEFAULT_BUFFER_SIZE, closeStream = true).use {
            it.write(data)
            it.flush()
        }
    }

    val out = ByteBuffer.alloc(300)
    file.channel(AccessType.READ).use {
        GZIPInput(it, DEFAULT_BUFFER_SIZE, closeStream = true).use {
            it.read(out)
        }
    }
    out.close()
}