package pw.binom.process

import pw.binom.atomic.AtomicBoolean
import kotlin.native.internal.createCleaner

class LinuxProcessStarter(
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

    private val io = IO()
    override val stdin
        get() = io.stdin
    override val stdout
        get() = io.stdout
    override val stderr
        get() = io.stderr

    @OptIn(ExperimentalStdlibApi::class)
    private val cleaner = createCleaner(io) {
        it.stdout.free()
        it.stderr.free()
        it.stdin.free()
    }

    private var processStarted = AtomicBoolean(false)

    override fun start(): Process {
        check(processStarted.compareAndSet(false, true)) { "Process already started!" }
        return LinuxProcess(this)
    }
}
