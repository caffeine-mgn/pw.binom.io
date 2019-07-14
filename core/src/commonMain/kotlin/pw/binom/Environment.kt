package pw.binom

object Environment

expect val Environment.platform: Platform
expect fun Environment.getEnv(name: String): String?
expect fun Environment.getEnvs(): Map<String,String>
