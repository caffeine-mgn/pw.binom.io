package pw.binom

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.toKString
import platform.posix.getenv

@OptIn(ExperimentalForeignApi::class)
actual fun Environment.getEnv(name: String): String? = getenv(name)?.toKString()

actual val Environment.userDirectory: String
  get() = getEnv("HOME") ?: ""
