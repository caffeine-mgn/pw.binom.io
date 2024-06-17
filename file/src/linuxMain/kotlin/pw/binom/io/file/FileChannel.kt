package pw.binom.io.file

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.convert
import kotlinx.cinterop.memScoped
import platform.posix.*
import pw.binom.atomic.AtomicBoolean
import pw.binom.io.ByteBuffer
import pw.binom.io.Channel
import pw.binom.io.ClosedException
import pw.binom.io.IOException

@OptIn(ExperimentalForeignApi::class)
actual class FileChannel actual constructor(
  file: File,
  mode: AccessMode,
) : Channel, RandomAccess {

  init {
    if (file.isDirectory) {
      throw IOException("Can't open folder ${file.path}")
    }
    if (!mode.isCreate && !file.isFile) {
      throw FileNotFoundException(file.path)
    }
  }

  internal val handler = fopen(
    file.path,
    run {
      val read =  mode.isRead
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
//          if (read) "cb+" else "cb"
          "ab"
        }

        read -> "rb"
        else -> throw IllegalArgumentException("Invalid mode")
      }
    },
  ) ?: run {
    println("errno=$errno")
    throw FileNotFoundException(file.path)
  }

  actual fun skip(length: Long): Long {
    checkClosed()
    memScoped { }
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

  override fun read(dest: ByteBuffer): Int {
    checkClosed()
    if (dest.remaining <= 0) {
      return 0
    }
    if (feof(handler) != 0) {
      return 0
    }

    val r = dest.ref(0) { destPtr, destRemaining ->
      fread(destPtr, 1.convert(), destRemaining.convert(), handler).convert<Int>()
    }
    dest.position += r
    return r
  }

//    override fun read(data: ByteDataBuffer, offset: Int, length: Int): Int {
//        if (feof(handler) != 0)
//            return 0
//        return fread(data.refTo(offset.convert()), 1.convert(), length.convert(), handler).convert()
//    }

  private val closed = AtomicBoolean(false)

  private fun checkClosed() {
    if (closed.getValue()) {
      throw ClosedException()
    }
  }

  override fun close() {
    if (!closed.compareAndSet(false, true)) {
      throw ClosedException()
    }
    fclose(handler)
  }

  override fun write(data: ByteBuffer): Int {
    checkClosed()
    if (feof(handler) != 0) {
      return 0
    }

    val r = data.ref(0) { dataPtr, dataRemaining ->
      fwrite(dataPtr, 1.convert(), dataRemaining.convert(), handler).convert<Int>()
    }
    data.position += r
    return r
  }

//    override fun write(data: ByteDataBuffer, offset: Int, length: Int): Int {
//        if (feof(handler) != 0)
//            return 0
//        return fwrite(data.refTo(offset), 1.convert(), length.convert(), handler).convert()
//    }

  override fun flush() {
    checkClosed()
    fflush(handler)
  }

  private fun gotoEnd() {
    fseek(handler, 0, SEEK_END)
  }

  override var position: Long
    get() = ftell(handler).convert()
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
