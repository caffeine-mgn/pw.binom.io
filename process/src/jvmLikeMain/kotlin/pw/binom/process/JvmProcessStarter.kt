package pw.binom.process

import pw.binom.io.ByteBuffer
import pw.binom.io.DataTransferSize
import pw.binom.io.Input
import pw.binom.io.Output
import java.io.File
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class JvmProcessStarter(
  val path: String,
  val args: List<String>,
  val envs: Map<String, String>,
  val workDir: String?,
) : ProcessStarter {
  private val lock = ReentrantLock()
  private val con = lock.newCondition()
  private var process: JvmProcess? = null
  override val stdin: Output = object : Output {
    override fun write(data: ByteBuffer): DataTransferSize {
      lock.withLock {
        if (process == null) {
          con.await()
        }
        val process = process ?: error("Process not ran")
        return process.internalStdin.write(data)
      }
    }

    override fun flush() {
      // Do nothing
    }

    override fun close() {
      // Do nothing
    }
  }
  override val stdout: Input = object : Input {
    override fun read(dest: ByteBuffer): DataTransferSize {
      lock.withLock {
        if (process == null) {
          con.await()
        }
        val process = process ?: error("Process not ran")
        return process.internalStdout.read(dest)
      }
    }

    override fun close() {
      // Do nothing
    }
  }
  override val stderr: Input = object : Input {
    override fun read(dest: ByteBuffer): DataTransferSize {
      lock.withLock {
        if (process == null) {
          con.await()
        }
        val process = process ?: error("Process not ran")
        return process.internalStderr.read(dest)
      }
    }

    override fun close() {
      // Do nothing
    }
  }

  override fun start(): Process {
    lock.withLock {
      if (this.process != null) {
        throw IllegalStateException("Process already started")
      }
      val pb = ProcessBuilder(listOf(path) + args)
      pb.environment().clear()
      pb.environment().putAll(envs)
      workDir?.let { pb.directory(File(it)) }
      pb.redirectOutput(ProcessBuilder.Redirect.PIPE)
      pb.redirectError(ProcessBuilder.Redirect.PIPE)
      val process = JvmProcess(process = pb.start(), processStarter = this)
      this.process = process
      con.signalAll()
      return process
    }
  }
}
