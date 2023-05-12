package pw.binom.process

import pw.binom.atomic.AtomicBoolean
import pw.binom.io.Input
import pw.binom.io.Output
import kotlin.native.internal.createCleaner

class MingwProcessStarter(
    val exe: String,
    val args: List<String>,
    val workDir: String?,
    val env: Map<String, String>,
) : ProcessStarter {

    class IO {
        val stdin = PipeOutput()
        val stdout = PipeInput()
        val stderr = PipeInput()
    }

    internal val io = IO()

    @OptIn(ExperimentalStdlibApi::class)
    private val cleaner = createCleaner(io) {
        it.stdout.close()
        it.stderr.close()
        it.stdin.close()
    }

    override val stdin: Output
        get() = io.stdin
    override val stdout: Input
        get() = io.stdout
    override val stderr: Input
        get() = io.stderr

    private val started = AtomicBoolean(false)

    override fun start(): Process {
        check(started.compareAndSet(false, true)) { "Process already started" }
        return WinProcess(this)
    }
}
