package pw.binom.io.file

import pw.binom.io.InputStream

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

val File.outputStream: FileOutputStream?
    get() {
        if (isDirectory || parent?.isDirectory==false)
            return null
        return FileOutputStream(this)
    }