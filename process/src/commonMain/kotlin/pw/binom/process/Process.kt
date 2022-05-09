package pw.binom.process

import pw.binom.Input
import pw.binom.Output
import pw.binom.io.Closeable

interface Process : Closeable {
    companion object

    val pid: Long
    val stdin: Output
    val stdout: Input
    val stderr: Input

    /**
     * Returns process exit statis
     *
     * @throws ProcessStillActive throw when process still running
     */
    val exitStatus: Int
    val isActive: Boolean

    fun join()

    class ProcessStillActive : RuntimeException()
}

expect fun Process.Companion.execute(
    path: String,
    args: List<String>,
    env: Map<String, String>,
    workDir: String?
): Process
