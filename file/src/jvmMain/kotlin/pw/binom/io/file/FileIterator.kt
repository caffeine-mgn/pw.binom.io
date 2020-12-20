package pw.binom.io.file

import pw.binom.io.Closeable
import pw.binom.io.IOException

actual class FileIterator internal actual constructor(val path: File) : Iterator<File>, Closeable {

    init {
        if (!path.isDirectory)
            throw IOException("\"${path.path}\" is not direction")
    }

    private val files = path.native.listFiles()
    private var cursor = 0
    override fun hasNext(): Boolean = cursor < files.size

    override fun next(): File {
        if (!hasNext())
            throw NoSuchElementException()
        return File(path, files[cursor++].name)
    }

    override fun close() {
        //NOP
    }

}