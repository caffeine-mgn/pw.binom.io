package pw.binom.io.pipe

import kotlinx.cinterop.*
import platform.posix.sleep
import platform.windows.*
import pw.binom.atomic.AtomicBoolean
import pw.binom.io.ByteBuffer
import pw.binom.io.Input

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
@OptIn(ExperimentalForeignApi::class)
actual class PipeInput private constructor(fd: Pair<HANDLE?, HANDLE?>) : Input {

  internal var writeFd = fd.first
  internal var readFd = fd.second

  val available: Int
    get() {
      memScoped {
        val totalAvailableBytes = alloc<UIntVar>()
        if (PeekNamedPipe(
            readFd,
            null,
            0.convert(),
            null,
            totalAvailableBytes.ptr,
            null,
          ) == 0
        ) {
          return -2
        }

        if (totalAvailableBytes.value > 0u) {
          return totalAvailableBytes.value.toInt()
        }

        TODO()
//                return if (process.isActive) -1 else 0
      }
    }

  private var endded = AtomicBoolean(false)

  actual constructor() : this(createPipe())

  actual constructor(output: PipeOutput) : this(output.writeFd to output.readFd)

  init {
    if (SetHandleInformation(readFd, HANDLE_FLAG_INHERIT.convert(), 0.convert()) <= 0) {
      TODO()
    }
  }

  override fun read(dest: ByteBuffer): Int {
    if (!dest.isReferenceAccessAvailable()) {
      return 0
    }
    while (true) {
      sleep(1.convert())
      if (available == 0) {
        return 0
      }

      if (available > 0) {
        break
      }
    }

    memScoped {
      val dwWritten = alloc<UIntVar>()

      val r = dest.refTo(dest.position) { destPtr ->
        ReadFile(
          readFd,
          (destPtr).getPointer(this).reinterpret(),
          dest.remaining.convert(),
          dwWritten.ptr,
          null,
        )
      } ?: 0
      if (r <= 0) {
        TODO()
      }
      val read = dwWritten.value.toInt()
      dest.position += read
      return read
    }
  }

  override fun close() {
    CloseHandle(readFd)
  }
}
