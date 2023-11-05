package pw.binom

actual val Environment.userDirectory: String
  get() = getEnv("HOME") ?: ""
