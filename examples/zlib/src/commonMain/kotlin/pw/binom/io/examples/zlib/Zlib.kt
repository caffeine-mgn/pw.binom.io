package pw.binom.io.examples.zlib

import pw.binom.asUTF8ByteArray
import pw.binom.asUTF8String
import pw.binom.io.ByteArrayOutputStream
import pw.binom.io.copyTo
import pw.binom.io.file.File
import pw.binom.io.file.FileInputStream
import pw.binom.io.file.FileOutputStream
import pw.binom.io.use
import pw.binom.io.zip.DeflaterOutputStream
import pw.binom.io.zip.InflateInputStream

fun main(args: Array<String>) {
    val sb = StringBuilder()
    for (i in 0..10) {
        sb.append("==>$i")
    }
    val data = sb.toString().asUTF8ByteArray()

    val LEVEL = 9
    val WRAP = true

    val file = File("Test.${if (WRAP) "wrap" else "nowrap"}")
    FileOutputStream(file, false).use {
        DeflaterOutputStream(it, LEVEL, 512, WRAP).use {
            it.write(data, 0, data.size)
            it.flush()
        }
    }

    val out = ByteArrayOutputStream(0)
    FileInputStream(file).use {
        InflateInputStream(it, 512, WRAP).use {
            it.copyTo(out)
        }
    }


    println("Result: ${out.toByteArray().asUTF8String()}")
}