package pw.binom

actual val Environment.platform: Platform
    get() = Platform.JS

actual fun Environment.getEnv(name: String): String? = null
actual fun Environment.getEnvs(): Map<String, String> = emptyMap()