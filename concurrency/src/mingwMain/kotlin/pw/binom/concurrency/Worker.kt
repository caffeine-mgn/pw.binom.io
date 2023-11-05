package pw.binom.concurrency

import kotlinx.cinterop.*
import platform.windows.GetSystemInfo
import platform.windows.SYSTEM_INFO

@OptIn(ExperimentalForeignApi::class)
actual val Worker.Companion.availableProcessors: Int
  get() =
    memScoped {
      val sysInfo = alloc<SYSTEM_INFO>()
      GetSystemInfo(sysInfo.ptr)
      sysInfo.dwNumberOfProcessors
    }.convert()
