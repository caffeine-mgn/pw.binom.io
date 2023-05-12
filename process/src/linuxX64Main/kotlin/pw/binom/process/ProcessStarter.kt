package pw.binom.process

internal actual fun createProcessStarter(
    path: String,
    args: List<String>,
    envs: Map<String, String>,
    workDir: String?,
): ProcessStarter = LinuxProcessStarter(exe = path, args = args, env = envs, workDir = workDir)
