package pw.binom.process

import pw.binom.atomic.AtomicBoolean
import pw.binom.concurrency.ReentrantLock
import pw.binom.concurrency.synchronize
import pw.binom.io.ByteBuffer
import pw.binom.io.Input
import pw.binom.io.pipe.PipeInput
import pw.binom.io.pipe.PipeOutput
import kotlin.experimental.ExperimentalNativeApi
import kotlin.native.ref.createCleaner

@OptIn(ExperimentalNativeApi::class)
class PosixProcessStarter(
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
  override val stdin
    get() = io.stdin
  override val stdout = object : Input {
    override fun read(dest: ByteBuffer) =
        io.stdout.read(dest)

    override fun close() {
      // do nothing
    }

  }
  override val stderr
    get() = io.stderr

  private val cleaner = createCleaner(io) {
    it.stdout.close()
    it.stderr.close()
    it.stdin.close()
  }

  private var processStarted = AtomicBoolean(false)

  override fun start(): Process {
    check(processStarted.compareAndSet(false, true)) { "Process already started!" }
    val r = PosixProcess(this)
    return r
  }
}
