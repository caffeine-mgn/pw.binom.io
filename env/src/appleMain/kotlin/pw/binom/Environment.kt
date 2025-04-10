@file:OptIn(UnsafeNumber::class, ExperimentalForeignApi::class, ExperimentalForeignApi::class)

package pw.binom

import kotlinx.cinterop.*
import platform.env.common.*
import platform.posix.*
import pw.binom.collections.defaultMutableMap
import platform.env.mac.*

actual fun Environment.getEnvs(): Map<String, String> {
  val out = defaultMutableMap<String, String>()
  var i = 0
  while (true) {
    val line = internal_getEnvs()?.get(i++)?.toKString() ?: break
    val items = line.split('=', limit = 2)
    out[items[0]] = items[1]
  }
  return out
}

@OptIn(ExperimentalForeignApi::class)
actual fun Environment.getEnv(name: String): String? = getenv(name)?.toKString()

@OptIn(ExperimentalForeignApi::class)
actual val Environment.workDirectory: String
  get() {
    val data = getcwd(null, 0.convert()) ?: TODO()
    if (errno == EACCES) {
      throw RuntimeException("Forbidden")
    }
    try {
      return data.toKString()
    } finally {
      free(data)
    }
  }

actual val Environment.userDirectory: String
  get() = getEnv("HOME") ?: ""


actual val Environment.currentTimeMillis: Long
  get() = memScoped {
      val ff = alloc<timespec>()
      clock_gettime(CLOCK_REALTIME.convert(), ff.ptr)
      ff.tv_sec * 1000L + ff.tv_nsec / 1000000L
  }

actual val Environment.currentExecutionPath: String
  get() =memScoped {
    val size = alloc<IntVar>()
    size.value = 0

    internal_getExecutablePath(null,size.ptr)
    val arr = allocArray<ByteVar>(size.value)
    internal_getExecutablePath(arr, size.ptr)
    arr.toKString()
  }

actual val Environment.currentTimeNanoseconds: Long
  get() = memScoped {
      val ff = alloc<timespec>()
      clock_gettime(CLOCK_PROCESS_CPUTIME_ID.convert(), ff.ptr)
      ff.tv_sec * 1000000000L + ff.tv_nsec
  }
