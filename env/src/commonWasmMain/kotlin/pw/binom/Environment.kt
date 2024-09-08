package pw.binom

actual val Environment.availableProcessors: Int
    get() = 1

actual val Environment.os: OS
    get() = OS.UNKNOWN

actual val Environment.platform: Platform
    get() = Platform.WASM

actual fun Environment.getEnv(name: String): String? = null
actual fun Environment.getEnvs(): Map<String, String> = emptyMap()
actual val Environment.isBigEndian: Boolean
    get() = true

actual val Environment.userDirectory: String
    get() = throw RuntimeException("Not supported")
