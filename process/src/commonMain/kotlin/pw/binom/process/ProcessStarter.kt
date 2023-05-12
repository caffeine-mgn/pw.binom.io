package pw.binom.process

import pw.binom.io.Input
import pw.binom.io.Output

interface ProcessStarter {
    val stdin: Output
    val stdout: Input
    val stderr: Input
    fun start(): Process

    companion object {
        fun create(
            path: String,
            args: List<String> = emptyList(),
            envs: Map<String, String> = emptyMap(),
            workDir: String? = null,
        ): ProcessStarter = createProcessStarter(path = path, args = args, envs = envs, workDir = workDir)
    }
}

internal expect fun createProcessStarter(
    path: String,
    args: List<String>,
    envs: Map<String, String>,
    workDir: String?,
): ProcessStarter
