package pw.binom

import kotlinx.cinterop.*
import platform.posix.*
import platform.windows.FreeEnvironmentStrings
import platform.windows.GetEnvironmentStringsW
import platform.windows.htonl
import platform.windows.lstrlen

val isBigEndianPrivate = htonl(47u) == 47u
actual val Environment.isBigEndian: Boolean
    get() = isBigEndianPrivate


actual val Environment.platform: Platform
    get() = Platform.MINGW_X64

actual fun Environment.getEnvs(): Map<String, String> {
    val out = HashMap<String, String>()
    val ff = GetEnvironmentStringsW()
    var vv = ff!!
    while (vv.pointed.value != 0.toUShort()) {
        val line = vv.toKString()
        vv = vv.plus(lstrlen!!.invoke(vv) + 1)!!
        val items = line.split('=', limit = 2)
        out[items[0]] = items[1]
    }
    FreeEnvironmentStrings!!.invoke(ff)
    return out
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

actual val Environment.workDirectory: String
    get() {
        val data = _wgetcwd(null, 0.convert()) ?: TODO()
        try {
            return data.toKString()
        } finally {
            free(data)
        }
    }