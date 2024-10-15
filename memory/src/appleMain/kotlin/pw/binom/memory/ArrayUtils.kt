package pw.binom.memory

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.usePinned
import platform.posix.memset
import platform.posix.memset

//@OptIn(ExperimentalForeignApi::class)
//actual fun ByteArray.fill(value: Byte) {
//  usePinned {
//    memset(it.addressOf(0), value.convert(), size.convert())
//  }
//}
