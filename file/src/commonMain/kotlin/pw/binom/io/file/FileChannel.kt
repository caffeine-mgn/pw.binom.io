package pw.binom.io.file

import pw.binom.io.Channel

expect class FileChannel(
  file: File,
  mode: AccessMode,
) : Channel, RandomAccess {
  fun skip(length: Long): Long
}

fun File.channel(mode: AccessMode): FileChannel {
  if (!mode.isCreate && !isFile) {
    throw FileNotFoundException(path)
  }
  return FileChannel(this, mode)
}

/**
 * Open file for read
 */
inline fun File.openRead() = channel(AccessMode.READ)

/**
 * Open file for write
 *
 * @param append flag for append exists file. Default - false
 */
inline fun File.openWrite(append: Boolean = false) =
  if (append) {
    channel(AccessMode.WRITE + AccessMode.CREATE + AccessMode.APPEND)
  } else {
    channel(AccessMode.WRITE + AccessMode.CREATE)
  }
