package pw.binom.process

import pw.binom.ByteBuffer
import pw.binom.Input
import pw.binom.Output
import java.io.File
import java.nio.channels.Channels

class JvmProcess(cmd: String, args: List<String>, workDir: String?, env: Map<String, String>) : Process {

    val process: java.lang.Process

    init {
        val pb = ProcessBuilder(listOf(cmd) + args)
        pb.environment().clear()
        pb.environment().putAll(env)
        workDir?.let { pb.directory(File(it)) }
        pb.redirectOutput(ProcessBuilder.Redirect.PIPE)
        pb.redirectError(ProcessBuilder.Redirect.PIPE)
        process = pb.start()
    }

    override val pid: Long
        get() = process.pid()
    override val stdin: Output = object :Output{

        private val channel = Channels.newChannel(process.outputStream)

        override fun write(data: ByteBuffer): Int =
                channel.write(data.native)

        override fun flush() {
        }

        override fun close() {
            channel.close()
        }

    }
    override val stdout: Input = ProcessInputStream(process, process.inputStream)
    override val stderr: Input = ProcessInputStream(process, process.errorStream)
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
        if (isActive)
            process.destroyForcibly()
    }

}

actual fun Process.Companion.execute(path: String, args: List<String>, env: Map<String, String>, workDir: String?): Process =
        JvmProcess(cmd = path, args = args.toList(), workDir = workDir, env = env)

private class ProcessInputStream(val process: java.lang.Process, val stream: java.io.InputStream) : Input {
    private val channel = Channels.newChannel(stream)

    override fun read(dest: ByteBuffer): Int {
        while (stream.available() == 0 && process.isAlive) {
            Thread.sleep(10)
        }
        return channel.read(dest.native)
    }

    override fun close() {
    }

}