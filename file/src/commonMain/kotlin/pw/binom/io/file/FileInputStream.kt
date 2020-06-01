@file:JvmName("FileInputStreamCommon")
package pw.binom.io.file

import pw.binom.io.InputStream
import kotlin.jvm.JvmName

expect class FileInputStream(file: File) : InputStream {
    override fun read(data: ByteArray, offset: Int, length: Int): Int
    override fun close()
}

val File.inputStream: FileInputStream?
    get() {
        if (!isFile)
            return null
        return FileInputStream(this)
    }

fun File.outputStream(append: Boolean = false): FileOutputStream? {
    if (isDirectory || parent?.isDirectory == false)
        return null
    return FileOutputStream(this, append)
}