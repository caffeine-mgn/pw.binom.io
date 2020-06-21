package pw.binom.io.examples.zlib

import pw.binom.DEFAULT_BUFFER_SIZE
import pw.binom.asUTF8ByteArray
import pw.binom.asUTF8String
import pw.binom.compression.zlib.DeflaterOutputStream
import pw.binom.compression.zlib.InflateInputStream
import pw.binom.io.ByteArrayOutputStream
import pw.binom.io.copyTo
import pw.binom.io.file.File
import pw.binom.io.file.FileInputStream
import pw.binom.io.file.FileOutputStream
import pw.binom.io.use

fun main(args: Array<String>) {
    val data = "Simple Text".asUTF8ByteArray()

    val LEVEL = 9
    val WRAP = true

    val file = File("Test.gz")
    FileOutputStream(file, false).use {
        DeflaterOutputStream(it, LEVEL, DEFAULT_BUFFER_SIZE, WRAP).use {
            it.write(data, 0, data.size)
            it.flush()
        }
    }

    val out = ByteArrayOutputStream()
    FileInputStream(file).use {
        InflateInputStream(it, DEFAULT_BUFFER_SIZE, WRAP).use {
            it.copyTo(out)
        }
    }


    println("Decompressed data: \"${out.toByteArray().asUTF8String()}\"")
}