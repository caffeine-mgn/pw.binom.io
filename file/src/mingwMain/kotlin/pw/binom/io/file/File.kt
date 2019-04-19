package pw.binom.io.file

import kotlinx.cinterop.alloc
import kotlinx.cinterop.convert
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import platform.posix.*

actual class File actual constructor(path: String) {
    actual constructor(parent: File, name: String) : this("${parent.parent}$SEPARATOR$name")

    actual val parent: File by lazy {
        val p = path.lastIndexOf(SEPARATOR)
        File(
                if (p == -1)
                    ""
                else
                    path.substring(0, p)
        )
    }

    actual val name: String
        get() {
            val p = path.lastIndexOf(SEPARATOR)
            if (p == -1)
                return ""
            return path.substring(p + 1)
        }

    actual val path: String = path

    actual val isFile: Boolean
        get() =
            memScoped {
                val stat = alloc<stat>()
                if (stat(path, stat.ptr) != 0)
                    return@memScoped false
                (S_IFDIR != (stat.st_mode and S_IFMT.convert()).convert<Int>())
            }

    actual val isDirectory: Boolean
        get() =
            memScoped {
                val stat = alloc<stat>()
                if (stat(path, stat.ptr) != 0)
                    return@memScoped false
                S_IFDIR == (stat.st_mode and S_IFMT.convert()).convert<Int>()
            }

    actual companion object {
        actual val SEPARATOR: Char
            get() = '\\'
    }

    actual fun delete(): Boolean {
        if (isDirectory)
            return rmdir(path) == 0

        if (isFile)
            return remove(path) == 0
        return false
    }

    actual fun mkdir(): Boolean = mkdir(path) == 0
}