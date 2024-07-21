package pw.binom.io.file

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.convert
import platform.posix.*
import platform.windows.GetLastError
import pw.binom.atomic.AtomicBoolean
import pw.binom.io.*

@OptIn(ExperimentalForeignApi::class)
actual class FileChannel actual constructor(
  file: File,
  mode: AccessMode,
) : Channel, RandomAccess {

  init {
    if (!mode.isCreate && !file.isFile) {
      throw FileNotFoundException(file.path)
    }
  }

  internal val handler = fopen(
    file.path,
    run {
      val read = mode.isRead
      val write = mode.isWrite
      val append = mode.isAppend
      if (!read && !write) {
        throw IllegalArgumentException("Invalid mode")
      }
      when {
        write && !append -> {
          if (read) "wb+" else "wb"
        }

        write && append -> {
          if (read) "cb+" else "cb"
        }

        read -> "rb"
        else -> throw IllegalArgumentException("Invalid mode")
      }
    },
  ) ?: throw IOException("Can't open file ${file.path}. Error: #${GetLastError()}")

  actual fun skip(length: Long): Long {
    checkClosed()
    if (length == 0L) {
      return 0L
    }
    if (feof(handler) != 0) {
      return 0L
    }
    val endOfFile = size
    val position = minOf(endOfFile, this.position + length)
    this.position = position
    return (endOfFile - position).toLong()
  }

  override fun read(dest: ByteBuffer): DataTransferSize {
    checkClosed()
    if (feof(handler) != 0) {
      return DataTransferSize.EMPTY
    }
    val l = dest.refTo(dest.position) { destPtr ->
      fread(destPtr, 1.convert(), dest.remaining.convert(), handler).convert<Int>()
    } ?: 0
    dest.position += l
    return DataTransferSize.ofSize(l)
  }

  private val closed = AtomicBoolean(false)
  private fun checkClosed() {
    if (closed.getValue()) {
      throw ClosedException()
    }
  }

  override fun close() {
    try {
      checkClosed()
      fclose(handler)
    } finally {
      closed.setValue(true)
    }
  }

  override fun write(data: ByteBuffer): DataTransferSize {
    checkClosed()
    if (feof(handler) != 0) {
      return DataTransferSize.EMPTY
    }
    val wroted = data.ref(0) { dataPtr, remaining ->
      fwrite(dataPtr, 1.convert(), remaining.convert(), handler).convert()
    }
    if (wroted > 0) {
      data.position += wroted
    }
    return DataTransferSize.ofSize(wroted)
  }

  override fun flush() {
    checkClosed()
    fflush(handler)
  }

  private fun gotoEnd() {
    checkClosed()
    fseek(handler, 0, SEEK_END)
  }

  override var position: Long
    get() {
      checkClosed()
      return ftell(handler).convert()
    }
    set(value) {
      fseek(handler, value.convert(), SEEK_SET)
    }
  override val size: Long
    get() {
      val pos = position
      gotoEnd()
      val result = position
      position = pos
      return result
    }
}
