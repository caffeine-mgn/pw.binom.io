package pw.binom.bluetooth

import com.sun.jna.Pointer
import pw.binom.io.ByteBuffer
import pw.binom.io.Channel
import pw.binom.io.DataTransferSize
import java.util.concurrent.atomic.AtomicBoolean

actual class SPPConnection(val native: Pointer) : Channel {

  private val closed = AtomicBoolean(false)

  override fun read(dest: ByteArray, offset: Int, length: Int): DataTransferSize {
    val wrote = NativeLibrary.INSTANCE.readFromSPP(
      connection = native,
      data = dest,
      offset = offset,
      dataSize = length,
    )
    return when {
      wrote > 0 -> DataTransferSize.ofSize(wrote)
      wrote == 0 -> DataTransferSize.EMPTY
      else -> DataTransferSize.CLOSED
    }
  }

  override fun read(dest: ByteBuffer): DataTransferSize {
    val wrote = if (dest.native.isDirect) {
      NativeLibrary.INSTANCE.readFromSPP(
        connection = native,
        data = dest.native,
        offset = dest.position,
        dataSize = dest.remaining,
      )
    } else {
      NativeLibrary.INSTANCE.readFromSPP(
        connection = native,
        data = dest.native.array(),
        offset = dest.position,
        dataSize = dest.remaining,
      )
    }
    return when {
      wrote > 0 -> {
        dest.position += wrote
        DataTransferSize.ofSize(wrote)
      }

      wrote == 0 -> DataTransferSize.EMPTY
      else -> DataTransferSize.CLOSED
    }
  }

  actual override fun close() {
    if (!closed.compareAndSet(false, true)) {
      return
    }
    NativeLibrary.INSTANCE.closeSPP(native)
  }

  override fun write(data: ByteArray, offset: Int, length: Int): DataTransferSize {
    val wrote = NativeLibrary.INSTANCE.writeToSPP(
      connection = native,
      data = data,
      offset = offset,
      dataSize = length,
    )
    return when {
      wrote > 0 -> DataTransferSize.ofSize(wrote)
      wrote == 0 -> DataTransferSize.EMPTY
      else -> DataTransferSize.CLOSED
    }
  }

  override fun write(data: ByteBuffer): DataTransferSize {
    val wrote = if (data.native.isDirect) {
      NativeLibrary.INSTANCE.writeToSPP(
        connection = native,
        data = data.native,
        offset = data.position,
        dataSize = data.remaining,
      )
    } else {
      NativeLibrary.INSTANCE.readFromSPP(
        connection = native,
        data = data.native.array(),
        offset = data.position,
        dataSize = data.remaining,
      )
    }
    return when {
      wrote > 0 -> {
        data.position += wrote
        DataTransferSize.ofSize(wrote)
      }

      wrote == 0 -> DataTransferSize.EMPTY
      else -> DataTransferSize.CLOSED
    }
  }

  override fun flush() {
  }
}
