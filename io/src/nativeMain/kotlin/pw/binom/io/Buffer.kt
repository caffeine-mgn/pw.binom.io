@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package pw.binom.io

import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi

actual interface Buffer : CommonBuffer {
  actual companion object;
  @OptIn(ExperimentalForeignApi::class)
  fun <T> refTo(position: Int, func: (CPointer<ByteVar>) -> T): T?
}
