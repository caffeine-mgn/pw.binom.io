package pw.binom.io.pipe

import com.jakewharton.cite.__FILE__
import com.jakewharton.cite.__LINE__
import com.jakewharton.cite.__TYPE__
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.UnsafeNumber
import kotlinx.cinterop.convert
import platform.posix.EBADF
import platform.posix.errno
import pw.binom.io.ByteBuffer
import pw.binom.io.DataTransferSize
import pw.binom.io.IOException
import pw.binom.io.Output

@OptIn(ExperimentalForeignApi::class, UnsafeNumber::class)
actual class PipeOutput private constructor(fd: PipePair) : Output {

  var writeFd: Int = fd.writeFd
    private set
  var readFd: Int = fd.readFd
    private set
  private var cloesd = false

  actual constructor() : this(PipePair())

  actual constructor(input: PipeInput) : this(PipePair(writeFd = input.writeFd, readFd = input.readFd))

  override fun write(data: ByteBuffer): DataTransferSize {
    if (cloesd) {
      return DataTransferSize.CLOSED
    }
    if (!data.isReferenceAccessAvailable()) {
      return DataTransferSize.EMPTY
    }
    val wrote = data.ref(0) { dataPtr, remaining ->
      if (remaining > 0) {
        platform.posix.write(writeFd, dataPtr, remaining.convert()).convert<Int>()
      } else {
        0
      }
    }
    if (wrote == -1) {
      if (errno == EBADF) {
        cloesd = true
        return DataTransferSize.CLOSED
      }
    }
    if (wrote == 0) {
      cloesd = true
      return DataTransferSize.CLOSED
    }
    data.position += wrote
    return DataTransferSize.ofSize(wrote)
  }

  override fun flush() {
    // Do nonthing
  }

  override fun close() {
    println("$__TYPE__: close fd=$writeFd ($__FILE__:$__LINE__)")
    platform.posix.close(writeFd)
  }
}
