package pw.binom.io.file

import kotlinx.cinterop.*
import platform.posix.*
import platform.windows.GetDiskFreeSpaceEx
import pw.binom.Environment
import pw.binom.collections.defaultArrayList
import pw.binom.getEnv
import kotlin.native.concurrent.freeze

actual class File actual constructor(path: String) {
    actual constructor(parent: File, name: String) : this(
        "${
        parent.path.removeSuffix("/").removeSuffix("\\")
        }$SEPARATOR${name.removePrefix("/").removePrefix("\\")}"
    )

    actual val path: String = replacePath(path)

    actual val isFile: Boolean
        get() =
            memScoped {
                val stat = alloc<stat>()
                if (stat(path, stat.ptr) != 0) {
                    return@memScoped false
                }
                (S_IFDIR != (stat.st_mode and S_IFMT.convert()).convert<Int>())
            }

    actual val isDirectory: Boolean
        get() =
            memScoped {
                val stat = alloc<stat>()
                if (stat(path, stat.ptr) != 0) {
                    return@memScoped false
                }
                S_IFDIR == (stat.st_mode and S_IFMT.convert()).convert<Int>()
            }

    actual companion object {
        actual val SEPARATOR: Char
            get() = '\\'
        actual val temporalDirectory: File?
            get() {
                val tmpDir = Environment.getEnv("TEMP") ?: Environment.getEnv("TMP")
                    ?: return null
                return File(tmpDir.removeSuffix("\\")).takeIfDirection()
            }
    }

    actual fun delete(): Boolean {
        if (isDirectory) {
            return rmdir(path) == 0
        }

        if (isFile) {
            return remove(path) == 0
        }
        return false
    }

    actual fun mkdir(): Boolean = mkdir(path) == 0

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
            val stat = alloc<_stat64>()
            if (_stat64(path, stat.ptr) != 0) {
                return@memScoped 0
            }
            return stat.st_size
        }

    actual val lastModified: Long
        get() = memScoped {
            val stat = alloc<_stat64>()
            if (_stat64(path, stat.ptr) != 0) {
                return@memScoped 0
            }
            return stat.st_ctime
        }

    actual fun renameTo(newPath: File): Boolean = rename(path, newPath.path) == 0

    actual fun list(): List<File> {
        val out = defaultArrayList<File>()
        iterator().forEach { file ->
            out += file
        }
        return out
    }

    actual val freeSpace: Long
        get() = memScoped {
            val value = alloc<ULongVar>()
            val fResult = GetDiskFreeSpaceEx!!(
                path.wcstr.ptr,
                null,
                null,
                value.ptr.reinterpret(),
            )
            if (fResult != 0) {
                value.value.toLong()
            } else {
                0L
            }
        }
    actual val availableSpace: Long
        get() = memScoped {
            val value = alloc<ULongVar>()
            val fResult = GetDiskFreeSpaceEx!!(
                path.wcstr.ptr,
                value.ptr.reinterpret(),
                null,
                null,
            )
            if (fResult != 0) {
                value.value.toLong()
            } else {
                0L
            }
        }

    actual val totalSpace: Long
        get() = memScoped {
            val value = alloc<ULongVar>()
            val fResult = GetDiskFreeSpaceEx!!(
                path.wcstr.ptr,
                null,
                null,
                value.ptr.reinterpret(),
            )
            if (fResult != 0) {
                value.value.toLong()
            } else {
                0L
            }
        }

    actual fun getPosixMode(): PosixPermissions = PosixPermissions(0u)

    actual fun setPosixMode(mode: PosixPermissions): Boolean = false
}
