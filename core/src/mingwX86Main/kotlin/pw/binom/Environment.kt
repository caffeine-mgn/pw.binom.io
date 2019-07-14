package pw.binom

import kotlinx.cinterop.*
import platform.windows.FreeEnvironmentStrings
import platform.windows.GetEnvironmentStringsW
import platform.windows.lstrlen

actual val Environment.platform: Platform
    get() = Platform.MINGW_X86

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