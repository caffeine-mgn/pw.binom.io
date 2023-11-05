package pw.binom

import kotlinx.cinterop.ExperimentalForeignApi
import platform.common.*

@OptIn(ExperimentalForeignApi::class)
actual fun threadYield() {
  internal_core_thread_yield()
}
