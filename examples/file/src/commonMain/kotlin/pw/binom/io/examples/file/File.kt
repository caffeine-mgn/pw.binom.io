package pw.binom.io.examples.file

import pw.binom.asUTF8ByteArray
import pw.binom.asUTF8String
import pw.binom.io.ByteArrayOutputStream
import pw.binom.io.copyTo
import pw.binom.io.file.*
import pw.binom.io.use
import pw.binom.io.zip.DeflaterOutputStream
import pw.binom.io.zip.InflateInputStream

fun main(args: Array<String>) {
    val data = "Simple Text".asUTF8ByteArray()

    val LEVEL = 9
    val WRAP = true

    val file = File("Test.zlib")
    FileOutputStream(file, false).use {
        DeflaterOutputStream(it, LEVEL, 512, WRAP).use {
            it.write(data, 0, data.size)
            it.flush()
        }
    }

    println("Compressed data: \"${data.asUTF8String()}\"")

    val out = ByteArrayOutputStream()
    FileInputStream(file).use {
        InflateInputStream(it, 512, WRAP).use {
            it.copyTo(out)
        }
    }


    println("Decompressed data: \"${out.toByteArray().asUTF8String()}\"")
}