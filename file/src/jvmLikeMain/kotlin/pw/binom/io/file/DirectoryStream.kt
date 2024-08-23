package pw.binom.io.file

import pw.binom.io.IOException

actual class DirectoryStream internal actual constructor(val path: File) : Iterator<File> {

    init {
        if (!path.isDirectory) {
            throw IOException("\"${path.path}\" is not direction")
        }
    }

    private val files = path.native.listFiles()
    private var cursor = 0
    actual override fun hasNext(): Boolean = cursor < files.size

    actual override fun next(): File {
        if (!hasNext()) {
            throw NoSuchElementException()
        }
        return File(path, files[cursor++].name)
    }
}
