package pw.binom.io.file

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.convert
import platform.posix.*
import pw.binom.atomic.AtomicBoolean
import pw.binom.io.ByteBuffer
import pw.binom.io.Channel
import pw.binom.io.ClosedException
import pw.binom.io.DataTransferSize

@OptIn(ExperimentalForeignApi::class)
actual class FileChannel actual constructor(
  file: File,
  mode: AccessMode,
) : Channel,
  RandomAccess {

  init {
    if (!mode.isCreate && !file.isFile) {
      throw FileNotFoundException(file.path)
    }
  }

  internal val handler = fopen(
    file.path,
    run {

      val append = mode.isAppend
      if (!mode.isRead && !mode.isWrite) {
        throw IllegalArgumentException("Invalid mode")
      }
      when {
        mode.isWrite && !append -> {
          if (mode.isRead) "wb+" else "wb"
        }

        mode.isWrite && append -> {
          if (mode.isRead) "cb+" else "cb"
        }

        mode.isRead -> "rb"
        else -> throw IllegalArgumentException("Invalid mode")
      }
    },
  ) ?: throw FileNotFoundException(file.path)

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

  actual override fun read(dest: ByteBuffer): DataTransferSize {
    checkClosed()
    if (feof(handler) != 0) {
      return DataTransferSize.EMPTY
    }
    val r = dest.ref(0) { destPtr, destRemaining ->
      fread(destPtr, 1.convert(), destRemaining.convert(), handler).convert<Int>()
    }
//        val r = fread(dest.refTo(dest.position), 1.convert(), dest.remaining.convert(), handler).convert<Int>()
    dest.position += r
    return DataTransferSize.ofSize(r)
  }

//    override fun read(data: ByteDataBuffer, offset: Int, length: Int): Int {
//        if (feof(handler) != 0)
//            return 0
//        return fread(data.refTo(offset.convert()), 1.convert(), length.convert(), handler).convert()
//    }

  actual override fun close() {
    try {
      checkClosed()
      fclose(handler)
    } finally {
      closed.setValue(true)
    }
  }

  actual override fun write(data: ByteBuffer): DataTransferSize {
    checkClosed()
    if (feof(handler) != 0) {
      return DataTransferSize.EMPTY
    }
    val r = data.ref(0) { dataPtr, dataRemaining ->
      fwrite(dataPtr, 1.convert(), dataRemaining.convert(), handler).convert<Int>()
    }
//        val r = fwrite(data.refTo(data.position), 1.convert(), data.remaining.convert(), handler).convert<Int>()
    data.position += r
    return DataTransferSize.ofSize(r)
  }

//    override fun write(data: ByteDataBuffer, offset: Int, length: Int): Int {
//        if (feof(handler) != 0)
//            return 0
//        return fwrite(data.refTo(offset), 1.convert(), length.convert(), handler).convert()
//    }

  actual override fun flush() {
    checkClosed()
    fflush(handler)
  }

  private fun gotoEnd() {
    checkClosed()
    fseek(handler, 0, SEEK_END)
  }

  actual override var position: Long
    get() {
      checkClosed()
      return ftell(handler).convert()
    }
    set(value) {
      checkClosed()
      fseek(handler, value.convert(), SEEK_SET)
    }
  actual override val size: Long
    get() {
      val pos = position
      gotoEnd()
      val result = position
      position = pos
      return result
    }

  private val closed = AtomicBoolean(false)
  private fun checkClosed() {
    if (closed.getValue()) {
      throw ClosedException()
    }
  }
}
