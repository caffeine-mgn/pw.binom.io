package pw.binom.io.file

import pw.binom.io.ByteBuffer
import pw.binom.io.Channel
import pw.binom.io.DataTransferSize
import pw.binom.io.StreamClosedException
import java.nio.channels.FileChannel
import java.nio.file.StandardOpenOption

actual class FileChannel actual constructor(file: File, mode: AccessMode) :
  Channel,
  RandomAccess {

  private val native: FileChannel

  init {
    val nativeMode = HashSet<StandardOpenOption>()
    mode.forEach {
      nativeMode += when (it) {
        AccessMode.APPEND -> StandardOpenOption.APPEND
        AccessMode.CREATE -> StandardOpenOption.CREATE
        AccessMode.READ -> StandardOpenOption.READ
        AccessMode.WRITE -> StandardOpenOption.WRITE
        else -> throw IllegalArgumentException()
      }
    }

    if (StandardOpenOption.APPEND !in nativeMode) {
      nativeMode += StandardOpenOption.TRUNCATE_EXISTING
    }

    native = FileChannel.open(
      file.native.toPath(),
      nativeMode,
    )
  }

  private var closed = false
  private fun checkClosed() {
    if (closed) {
      throw StreamClosedException()
    }
  }

  actual fun skip(length: Long): Long {
    checkClosed()
    val l = minOf(native.position() + length, native.size())
    native.position(l)
    return l
  }

  override fun read(dest: ByteBuffer): DataTransferSize {
    checkClosed()
    return native.read(dest.native).let {
      if (it == -1) DataTransferSize.EMPTY else DataTransferSize.ofSize(it)
    }
  }

//    override fun read(data: ByteDataBuffer, offset: Int, length: Int): Int {
//        return data.update(offset, length) { data ->
//            native.read(data)
//        }
//    }

  override fun close() {
    if (closed) {
      return
    }
    closed = true
    native.close()
  }

  override fun write(data: ByteBuffer): DataTransferSize {
    checkClosed()
    return DataTransferSize.ofSize(native.write(data.native))
  }

//    override fun write(data: ByteDataBuffer, offset: Int, length: Int): Int {
//        return data.update(offset, length) { data ->
//            native.write(data)
//        }
//    }

  override fun flush() {
    checkClosed()
    native.force(true)
  }

  override var position: Long
    get() = native.position()
    set(value) {
      checkClosed()
      native.position(value)
    }

  override val size: Long
    get() {
      checkClosed()
      return native.size()
    }
}
