package pw.binom

import kotlinx.cinterop.*
import platform.posix.*

actual val Environment.os: OS
    get() = OS.LINUX

actual val Environment.isBigEndian: Boolean
    get() = isBigEndianPrivate

actual val Environment.platform: Platform
    get() = Platform.LINUX_MIPS_32

actual fun Environment.getEnvs(): Map<String, String> {
    val out = HashMap<String, String>()
    var i = 0
    while (true) {
        val line = (__environ!!.get(i++) ?: break).toKString()
        val items = line.split('=', limit = 2)
        out[items[0]] = items[1]
    }
    return out
}

actual val Environment.workDirectory: String
    get() {
        val data = getcwd(null, 0.convert()) ?: TODO()
        if (errno == EACCES)
            throw RuntimeException("Forbidden")
        try {
            return data.toKString()
        } finally {
            free(data)
        }
    }

actual val Environment.currentTimeMillis: Long
    get() = memScoped {
        val ff = alloc<timespec>()
        clock_gettime(CLOCK_REALTIME, ff.ptr)
        ff.tv_sec * 1000L + ff.tv_nsec / 1000000L
    }

actual val Environment.currentTimeNanoseconds: Long
    get() = memScoped {
        val ff = alloc<timespec>()
        clock_gettime(CLOCK_PROCESS_CPUTIME_ID, ff.ptr)
        ff.tv_sec * 1000000000L + ff.tv_nsec
    }

actual fun Environment.getEnv(name: String): String? = getenv(name)?.toKString()

actual val Environment.userDirectory: String
    get() = getEnv("HOME") ?: ""

actual val Environment.currentExecutionPath: String
    get() =
        memScoped {
            val result = allocArray<ByteVar>(PATH_MAX)
            val count = readlink("/proc/self/exe", result, PATH_MAX).convert<Int>()
            if (count != -1) {
                result.toKString()
            } else {
                throw RuntimeException("Can't get current execution path. Error #$errno")
            }
        }
