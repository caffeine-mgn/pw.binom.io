package pw.binom.io.file

import pw.binom.io.ByteBuffer
import pw.binom.io.Channel
import pw.binom.io.DataTransferSize

expect class FileChannel(
  file: File,
  mode: AccessMode,
) : Channel, RandomAccess {
  override fun close()
  override var position: Long
  override val size: Long
  override fun flush()
  override fun read(dest: ByteBuffer): DataTransferSize
  override fun write(data: ByteBuffer): DataTransferSize
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
@Suppress("NOTHING_TO_INLINE")
inline fun File.openRead() = channel(AccessMode.READ)

/**
 * Open file for write
 *
 * @param append flag for append exists file. Default - false
 */
@Suppress("NOTHING_TO_INLINE")
inline fun File.openWrite(append: Boolean = false) =
  if (append) {
    channel(AccessMode.WRITE + AccessMode.CREATE + AccessMode.APPEND)
  } else {
    channel(AccessMode.WRITE + AccessMode.CREATE)
  }
