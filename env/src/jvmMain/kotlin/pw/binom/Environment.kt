package pw.binom

actual val Environment.platform: Platform
    get() = Platform.JVM

actual fun Environment.getEnv(name: String): String? = System.getenv(name)
actual fun Environment.getEnvs(): Map<String, String> = System.getenv()
actual val Environment.isBigEndian: Boolean
    get() = true
actual val Environment.workDirectory: String
    get() = System.getProperty("user.dir")

actual val Environment.currentTimeMillis: Long
    get() = System.currentTimeMillis()

actual val Environment.currentTimeNanoseconds: Long
    get() = System.nanoTime()

actual val Environment.userDirectory: String
    get() = System.getProperty("user.home")