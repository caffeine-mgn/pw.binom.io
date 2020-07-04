package pw.binom.io.examples.file

import pw.binom.ByteBufferPool
import pw.binom.asUTF8String
import pw.binom.copyTo
import pw.binom.io.ByteArrayOutput
import pw.binom.io.file.AccessType
import pw.binom.io.file.File
import pw.binom.io.file.channel
import pw.binom.io.use
import pw.binom.toByteBufferUTF8

fun main(args: Array<String>) {
    val data = "Simple Text".toByteBufferUTF8()
    val bufferPool = ByteBufferPool()
    val file = File("Simple File")
    file.channel(AccessType.WRITE, AccessType.CREATE).use {
        it.write(data)
        it.flush()
    }
    data.clear()
    println("Write data: \"${data.asUTF8String()}\"")

    val out = ByteArrayOutput()
    file.channel(AccessType.READ).use {
        it.copyTo(out, bufferPool)
    }
    out.trimToSize()
    out.data.clear()

    println("Read data: \"${out.data.asUTF8String()}\"")
    out.close()
    bufferPool.close()
}