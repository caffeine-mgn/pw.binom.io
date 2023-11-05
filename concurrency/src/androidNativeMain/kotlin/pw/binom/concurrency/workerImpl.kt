package pw.binom.concurrency

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.UnsafeNumber
import kotlinx.cinterop.convert
import platform.posix._SC_NPROCESSORS_ONLN
import platform.posix.sysconf

@OptIn(ExperimentalForeignApi::class, UnsafeNumber::class)
actual val Worker.Companion.availableProcessors: Int
  get() = sysconf(_SC_NPROCESSORS_ONLN).convert()
