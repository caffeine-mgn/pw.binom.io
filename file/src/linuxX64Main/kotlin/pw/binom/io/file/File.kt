package pw.binom.io.file

import kotlinx.cinterop.*
import platform.linux.statvfs
import platform.posix.*
import kotlin.native.concurrent.freeze
import pw.binom.io.*

private fun timespec.toMillis(): Long {
    var s = tv_sec

    var ms = round(tv_nsec / 1.0e6).toLong()
    if (ms > 999) {
        s++
        ms = 0
    }
    return s * 1000 + ms
}

actual class File actual constructor(path: String) {
    actual constructor(parent: File, name: String) : this("${parent.path.removeSuffix("/").removeSuffix("\\")}$SEPARATOR${name.removePrefix("/").removePrefix("\\")}")

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

    actual override fun toString(): String = path
    actual override fun equals(other: Any?): Boolean {
        if (other !is File) return false
        return path == other.path
    }

    actual override fun hashCode(): Int = 31 + path.hashCode()

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

    actual fun renameTo(newPath: File): Boolean = rename(path, newPath.path) == 0

    actual fun list(): List<File> {
        val out = ArrayList<File>()
        iterator().use {
            it.forEach { file ->
                out += file
            }
        }
        return out
    }

    actual val freeSpace: Long
        get() =
            memScoped {
                val stat = alloc<statvfs>()
                statvfs(path, stat.ptr)
                (stat.f_bfree.toULong() * stat.f_bsize.toULong()).toLong()
            }

    actual val availableSpace: Long
        get() =
            memScoped {
                val stat = alloc<statvfs>()
                statvfs(path, stat.ptr)
                (stat.f_bavail.toULong() * stat.f_bsize.toULong()).toLong()
            }

    actual val totalSpace: Long
        get() = memScoped {
            val stat = alloc<statvfs>()
            statvfs(path, stat.ptr)
            (stat.f_blocks.toULong() * stat.f_frsize.toULong()).toLong()
        }
}