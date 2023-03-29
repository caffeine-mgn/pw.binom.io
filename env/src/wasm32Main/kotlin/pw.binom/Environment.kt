package pw.binom

// actual val Environment.availableProcessors: Int
//    get() = 1

actual fun Environment.getEnv(name: String): String? = null
actual fun Environment.getEnvs(): Map<String, String> = emptyMap()
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
