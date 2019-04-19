package pw.binom.io.file

import kotlinx.cinterop.pointed
import kotlinx.cinterop.toKString
import platform.posix.closedir
import platform.posix.dirent
import platform.posix.opendir
import platform.posix.readdir
import pw.binom.io.Closeable
import pw.binom.io.IOException

actual class FileIterator internal actual constructor(path: File) : Iterator<File>, Closeable {

    init {
        if (!path.isDirectory)
            throw IOException("\"${path.path}\" is not direction")
    }

    private val handler = opendir(path.path)
    private var next: dirent? = null
    private var end = false

    override fun hasNext(): Boolean {
        if (end)
            return false

        if (next == null) {
            next = readdir(handler)?.pointed
            if (next == null) {
                end = true
                return false
            }
            return true
        }
        return true
    }

    override fun next(): File {
        if (!hasNext())
            throw NoSuchElementException()
        val result = File(next!!.d_name.toKString())
        next = null
        return result
    }

    override fun close() {
        closedir(handler)
    }
}