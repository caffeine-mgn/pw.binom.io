package pw.binom.io.pipe

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.UnsafeNumber
import kotlinx.cinterop.convert
import platform.posix.EAGAIN
import platform.posix.errno
import platform.posix.usleep
import pw.binom.atomic.AtomicBoolean
import pw.binom.io.ByteBuffer
import pw.binom.io.DataTransferSize
import pw.binom.io.Input
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalForeignApi::class, UnsafeNumber::class)
actual class PipeInput private constructor(fd: PipePair) : Input {

  var writeFd: Int = fd.writeFd
    private set
  var readFd: Int = fd.readFd
    private set

  private var endded = AtomicBoolean(false)

  actual constructor() : this(PipePair())

  actual constructor(output: PipeOutput) : this(PipePair(writeFd = output.writeFd, readFd = output.readFd))

  override fun read(dest: ByteBuffer): DataTransferSize {
    var readed = 0
    while (true) {
      if (endded.getValue()) {
        return DataTransferSize.CLOSED
      }

      val r = dest.ref(0) { destPtr, remaining ->
        if (remaining > 0) {
          platform.posix.read(readFd, destPtr, remaining.convert()).convert<Int>()
        } else {
          0
        }
      }
      if (r == -1) {
        if (errno == EAGAIN) {
          usleep(10.milliseconds.inWholeMicroseconds.toUInt())
          continue
        }
        TODO()
      }
      if (r == 0) {
        endded.setValue(true)
        return if (readed == 0) {
          DataTransferSize.CLOSED
        } else {
          DataTransferSize.ofSize(readed)
        }
      }
      dest.position += r
      readed += r
      return DataTransferSize.ofSize(readed)
    }
  }

  override fun close() {
    platform.posix.close(readFd)
  }
}
