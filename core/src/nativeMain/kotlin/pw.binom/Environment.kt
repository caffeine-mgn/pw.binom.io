package pw.binom

import kotlinx.cinterop.toKString
import platform.posix.*

actual fun Environment.getEnv(name: String): String? = getenv(name)?.toKString()