package pw.binom

object Environment

expect val Environment.platform: Platform
expect fun Environment.getEnv(name: String): String?
expect fun Environment.getEnvs(): Map<String, String>
expect val Environment.isBigEndian: Boolean
expect val Environment.workDirectory: String
expect val Environment.currentTimeMillis: Long
expect val Environment.currentTimeNanoseconds: Long