package pw.binom.memory

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.memory.common.internal_memset

@OptIn(ExperimentalForeignApi::class)
actual fun ByteArray.fill(value: Byte) {
  usePinned {
    internal_memset(it.addressOf(0), size.toLong(), value)
  }
}
