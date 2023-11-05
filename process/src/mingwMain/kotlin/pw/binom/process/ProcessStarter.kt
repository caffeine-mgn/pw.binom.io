package pw.binom.process

internal actual fun createProcessStarter(
  path: String,
  args: List<String>,
  envs: Map<String, String>,
  workDir: String?,
): ProcessStarter = MingwProcessStarter(
  exe = path,
  args = args,
  workDir = workDir,
  env = envs,
)
