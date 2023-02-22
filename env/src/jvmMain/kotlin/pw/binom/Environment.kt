package pw.binom

import java.io.File

actual val Environment.availableProcessors: Int
    get() = Runtime.getRuntime().availableProcessors()

actual val Environment.os: OS
    get() {
        val os = System.getProperty("os.name").lowercase()
        return when {
            "windows" in os -> OS.WINDOWS
            "nux" in os -> OS.LINUX
            "mac" in os || "darwin" in os -> OS.MACOS
            "android" in os -> OS.ANDROID
            else -> OS.UNKNOWN
        }
    }

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

actual val Environment.currentExecutionPath: String
    get() = File(Environment::class.java.protectionDomain.codeSource.location.toURI()).path
