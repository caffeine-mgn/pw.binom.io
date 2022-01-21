package pw.binom

actual val Environment.os: OS
    get() = OS.ANDROID

actual val Environment.userDirectory: String
    get() = getEnv("HOME") ?: ""