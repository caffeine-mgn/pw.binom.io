package pw.binom.process

import pw.binom.io.InputStream
import pw.binom.io.OutputStream
import pw.binom.io.wrap
import java.io.File

class JvmProcess(cmd: String, args: List<String>, workDir: String?, env: Map<String, String>) : Process {

    val process: java.lang.Process

    init {
        println("create proces... $cmd [$args]. env=$env")
        val pb = ProcessBuilder(listOf(cmd) + args)
        pb.environment().clear()
        pb.environment().putAll(env)
        workDir?.let { pb.directory(File(it)) }
        pb.redirectOutput(ProcessBuilder.Redirect.PIPE)
        pb.redirectError(ProcessBuilder.Redirect.PIPE)
        println("execute process...")
        process = pb.start()
        println("process started! ${process.isAlive}")
    }

    override val pid: Long
        get() = process.pid()
    override val stdin: OutputStream = process.outputStream.wrap()
    override val stdout: InputStream = ProcessInputStream(process, process.inputStream)
    override val stderr: InputStream = ProcessInputStream(process, process.errorStream)
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

private class ProcessInputStream(val process: java.lang.Process, val stream: java.io.InputStream) : InputStream {

    override val available: Int
        get() {
            val r = stream.available()
            if (r <= 0 && process.isAlive)
                return -1
            return r
        }

    override fun read(data: ByteArray, offset: Int, length: Int): Int {
        while (stream.available() == 0 && process.isAlive) {
            Thread.sleep(10)
        }
        return stream.read(data, offset, length)
    }

    override fun close() {
    }

}