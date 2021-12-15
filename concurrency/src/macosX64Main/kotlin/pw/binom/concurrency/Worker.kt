package pw.binom.concurrency

import kotlinx.cinterop.convert
import platform.posix._SC_NPROCESSORS_ONLN
import platform.posix.sysconf

actual val Worker.Companion.availableProcessors: Int
    get() = sysconf(_SC_NPROCESSORS_ONLN).convert()