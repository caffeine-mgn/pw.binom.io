package pw.binom.io.file

import kotlinx.cinterop.*
import platform.common.internal_get_available_space
import platform.common.internal_get_free_space
import platform.common.internal_get_total_space
import platform.posix.*
import pw.binom.collections.defaultMutableList
import pw.binom.io.IOException
import kotlin.native.concurrent.freeze

private fun timespec.toMillis(): Long {
  var s = tv_sec

  var ms = round(tv_nsec / 1.0e6).toLong()
  if (ms > 999) {
    s++
    ms = 0
  }
  return s * 1000 + ms
}

@OptIn(ExperimentalForeignApi::class)
actual class File actual constructor(path: String) {
  actual constructor(parent: File, name: String) : this(
    "${
      parent.path.removeSuffix("/").removeSuffix("\\")
    }$SEPARATOR${name.removePrefix("/").removePrefix("\\")}",
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
      get() = '/'
    actual val temporalDirectory: File?
      get() = File("/tmp").takeIfDirection()
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

  actual fun mkdir(): Boolean = mkdir(path, ACCESSPERMS.convert()) == 0

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
      if (stat(path, stat.ptr) != 0) {
        return@memScoped 0
      }
      return stat.st_size.convert()
    }
  actual val lastModified: Long
    get() = memScoped {
      val stat = alloc<stat>()
      if (stat(path, stat.ptr) != 0) {
        return@memScoped 0
      }
      return stat.st_ctim.toMillis()
    }

  actual fun renameTo(newPath: File): Boolean = rename(path, newPath.path) == 0

  actual fun list(): List<File> {
    val out = defaultMutableList<File>()
    iterator().forEach { file ->
      out += file
    }
    return out
  }

  actual val freeSpace: Long
    get() = internal_get_free_space(path)

  actual val availableSpace: Long
    get() = internal_get_available_space(path)

  actual val totalSpace: Long
    get() = internal_get_total_space(path)

  actual fun getPosixMode(): PosixPermissions =
    memScoped {
      val stat = alloc<stat>()
      if (stat(path, stat.ptr) != 0) {
        return@memScoped PosixPermissions(0u)
      }
      return PosixPermissions(stat.st_mode)
    }

  actual fun setPosixMode(mode: PosixPermissions): Boolean {
    if (chmod(path, mode.mode.convert()) != 0) {
      throw IOException("Can't change mode of file \"$path\"")
    }
    return true
  }

  actual fun createSymbolicLink(to: File) {
    if (!isExist) {
      throw FileNotFoundException("$path not found")
    }
    if (symlink(path, to.path) != 0) {
      throw IOException("Can't create symbolic from $path to ${to.path}. Error: $errno")
    }
  }
}
