package pw.binom.bluetooth

import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.bluetooth.*
import pw.binom.io.ByteBuffer
import pw.binom.io.Channel
import pw.binom.io.DataTransferSize
import kotlin.concurrent.AtomicInt

@OptIn(ExperimentalForeignApi::class)
actual class SPPConnection(val native: CPointer<NSPPConnection>) : Channel {
  private val closed = AtomicInt(0)
  actual override fun close() {
    if (!closed.compareAndSet(0, 1)) {
      return
    }
    closeSPPConnection(native)
  }

  actual override fun write(data: ByteBuffer): DataTransferSize {
    if (!data.hasRemaining) {
      DataTransferSize.EMPTY
    }
    return data.ref(DataTransferSize.EMPTY) { pointer, remaining ->
      DataTransferSize.ofSize(
        writeToSPP(
          connection = native,
          data = pointer,
          dataSize = remaining,
          offset = 0,
        )
      )
    }
  }

  override fun write(data: ByteArray, offset: Int, length: Int): DataTransferSize =
    data.usePinned { dataPinned ->
      DataTransferSize.ofSize(
        writeToSPP(
          connection = native,
          data = dataPinned.addressOf(0),
          dataSize = length,
          offset = offset,
        )
      )
    }

  actual override fun flush() {

  }

  actual override fun read(dest: ByteBuffer): DataTransferSize {
    if (!dest.hasRemaining) {
      DataTransferSize.EMPTY
    }
    return dest.ref(DataTransferSize.EMPTY) { pointer, remaining ->
      DataTransferSize.ofSize(
        readFromSPP(
          connection = native,
          data = pointer,
          dataSize = remaining,
          offset = 0,
        )
      )
    }
  }

  override fun read(dest: ByteArray, offset: Int, length: Int): DataTransferSize =
    dest.usePinned { dataPinned ->
      DataTransferSize.ofSize(
        readFromSPP(
          connection = native,
          data = dataPinned.addressOf(0),
          dataSize = length,
          offset = offset,
        )
      )
    }
}
