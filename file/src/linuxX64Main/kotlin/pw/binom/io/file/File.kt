package pw.binom.io.file

import kotlinx.cinterop.alloc
import kotlinx.cinterop.convert
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import platform.posix.*
import pw.binom.toMillis
import kotlin.native.concurrent.freeze

actual class File actual constructor(path: String) {
    actual constructor(parent: File, name: String) : this("${parent.path.removeSuffix("/").removeSuffix("\\")}$SEPARATOR$name")

    actual val path: String = replacePath(path)

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
            get() = '/'
    }

    actual fun delete(): Boolean {
        if (isDirectory)
            return rmdir(path) == 0

        if (isFile)
            return remove(path) == 0
        return false
    }

    actual fun mkdir(): Boolean = mkdir(path, ACCESSPERMS) == 0

    override fun toString(): String = path

    init {
        freeze()
    }

    actual val size: Long
        get() = memScoped {
            val stat = alloc<stat>()
            if (stat(path, stat.ptr) != 0)
                return@memScoped 0
            return stat.st_size.convert()
        }
    actual val lastModified: Long
        get() = memScoped {
            val stat = alloc<stat>()
            if (stat(path, stat.ptr) != 0)
                return@memScoped 0
            return stat.st_ctim.toMillis()
        }
}