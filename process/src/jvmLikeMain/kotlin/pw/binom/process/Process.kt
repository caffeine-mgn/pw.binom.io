package pw.binom.process

import pw.binom.io.ByteBuffer
import pw.binom.io.DataTransferSize
import pw.binom.io.Input
import pw.binom.io.Output
import java.nio.channels.Channels

class JvmProcess(val process: java.lang.Process, val processStarter: JvmProcessStarter) : Process {

  override val pid: Long
    get() = process.pid()
  internal val internalStdin: Output = object : Output {

    private val channel = Channels.newChannel(process.outputStream)

    override fun write(data: ByteBuffer) = DataTransferSize.ofSize(channel.write(data.native))

    override fun flush() {
      // Do nothing
    }

    override fun close() {
      channel.close()
    }
  }
  override val stdin: Output
    get() = processStarter.stdin
  internal val internalStdout: Input = ProcessInputStream(process, process.inputStream)
  override val stdout
    get() = processStarter.stdout
  internal val internalStderr: Input = ProcessInputStream(process, process.errorStream)
  override val stderr
    get() = processStarter.stderr
  override val exitStatus: Int
    get() {
      try {
        return process.exitValue()
      } catch (e: IllegalThreadStateException) {
        throw Process.ProcessStillActive()
      }
    }
  override val isActive: Boolean
    get() = process.isAlive

  override fun join() {
    process.waitFor()
  }

  override fun close() {
    process.destroy()
    if (isActive) {
      process.destroyForcibly()
    }
  }
}

private class ProcessInputStream(val process: java.lang.Process, val stream: java.io.InputStream) : Input {
  private val channel = Channels.newChannel(stream)

  override fun read(dest: ByteBuffer): DataTransferSize {
    while (stream.available() == 0 && process.isAlive) {
      Thread.sleep(10)
    }
    return DataTransferSize.ofSize(channel.read(dest.native))
  }

  override fun close() {
  }
}
