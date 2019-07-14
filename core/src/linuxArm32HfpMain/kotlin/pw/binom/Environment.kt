package pw.binom

import kotlinx.cinterop.get
import kotlinx.cinterop.toKString
import platform.posix.__environ

actual val Environment.platform: Platform
    get() = Platform.LINUX_ARM_32

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