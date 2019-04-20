package pw.binom.io.examples.file

import pw.binom.asUTF8ByteArray
import pw.binom.asUTF8String
import pw.binom.io.ByteArrayOutputStream
import pw.binom.io.copyTo
import pw.binom.io.file.File
import pw.binom.io.file.FileInputStream
import pw.binom.io.file.FileOutputStream
import pw.binom.io.use

fun main(args: Array<String>) {
    val data = "Simple Text".asUTF8ByteArray()

    val file = File("Simple File")
    FileOutputStream(file, false).use {
        it.write(data, 0, data.size)
        it.flush()
    }

    println("Write data: \"${data.asUTF8String()}\"")

    val out = ByteArrayOutputStream()
    FileInputStream(file).use {
        it.copyTo(out)
    }


    println("Read data: \"${out.toByteArray().asUTF8String()}\"")
}