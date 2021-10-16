package pw.binom

import kotlinx.cinterop.*
import platform.osx._NSGetEnviron
import platform.posix.*

/**
 * constexpr endian_t getEndianOrder() {
return
((0xFFFFFFFF & 1) == LITTLE_ENDIAN)
? LITTLE_ENDIAN
: ((0xFFFFFFFF & 1) == BIG_ENDIAN)
? BIG_ENDIAN
: ((0xFFFFFFFF & 1) == PDP_ENDIAN)
? PDP_ENDIAN
: UNKNOWN_ENDIAN;
}
 */

actual val Environment.os: OS
    get() = OS.MACOS

actual val Environment.isBigEndian: Boolean
    get() = isBigEndianPrivate

actual val Environment.platform: Platform
    get() = Platform.MACOS

actual fun Environment.getEnvs(): Map<String, String> {
    val out = HashMap<String, String>()
    var i = 0
    val envs = _NSGetEnviron()
    while (true) {
        val line = (envs!!.get(i++) ?: break).pointed.value!!.toKString()
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