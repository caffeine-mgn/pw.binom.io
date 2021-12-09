package pw.binom

import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.*

actual val Environment.os: OS
    get() =OS.WEB

actual val Environment.platform: Platform
    get() = Platform.WASM32

actual fun Environment.getEnv(name: String): String? = null
actual fun Environment.getEnvs(): Map<String, String> = emptyMap()
actual val Environment.isBigEndian: Boolean
    get() = true
actual val Environment.workDirectory: String
    get() = ""

actual val Environment.currentTimeMillis: Long
    get() = 0L

actual val Environment.currentTimeNanoseconds: Long
    get() = 0L


actual val Environment.userDirectory: String
    get() = ""

actual val Environment.currentExecutionPath: String
    get() = ""