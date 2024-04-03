package pw.binom.io.file

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.pointed
import kotlinx.cinterop.toKString
import platform.posix.closedir
import platform.posix.dirent
import platform.posix.opendir
import platform.posix.readdir
import pw.binom.io.IOException
import kotlin.experimental.ExperimentalNativeApi
import kotlin.native.concurrent.ensureNeverFrozen
import kotlin.native.ref.createCleaner

@OptIn(ExperimentalForeignApi::class)
actual class DirectoryStream internal actual constructor(private val path: File) : Iterator<File> {

  init {
    if (!path.isDirectory) {
      throw IOException("\"${path.path}\" is not direction")
    }
  }

  private val handler = opendir(path.path)
  private var next: dirent? = null
  private var end = false

  override fun hasNext(): Boolean {
    while (true) {
      if (end) {
        return false
      }

      if (next == null) {
        next = readdir(handler)?.pointed
        if (next == null) {
          end = true
          return false
        }
        val name = next!!.d_name.toKString()
        if (name == "." || name == "..") {
          next = null
          continue
        }
        return true
      }
      return true
    }
  }

  override fun next(): File {
    if (!hasNext()) {
      throw NoSuchElementException()
    }
    val result = File(path, next!!.d_name.toKString())
    next = null
    return result
  }

  @OptIn(ExperimentalNativeApi::class)
  private val cleaner = createCleaner(handler) {
    closedir(it)
  }
}
