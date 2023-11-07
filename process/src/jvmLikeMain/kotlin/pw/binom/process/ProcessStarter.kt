package pw.binom.process

internal actual fun createProcessStarter(
  path: String,
  args: List<String>,
  envs: Map<String, String>,
  workDir: String?,
): ProcessStarter = JvmProcessStarter(path = path, args = args, envs = envs, workDir = workDir)
