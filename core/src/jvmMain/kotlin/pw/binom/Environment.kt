package pw.binom

actual val Environment.platform: Platform
    get() = Platform.JVM

actual fun Environment.getEnv(name: String): String? = System.getenv(name)
actual fun Environment.getEnvs(): Map<String, String> = System.getenv()