package pw.binom.process

import pw.binom.io.InputStream
import pw.binom.io.OutputStream
import pw.binom.io.wrap
import java.io.File

class JvmProcess(cmd: String, args: List<String>, workDir: String?) : Process {
    val process: java.lang.Process

    init {
        val pb = ProcessBuilder(listOf(cmd) + args)
        workDir?.let { pb.directory(File(it)) }
//        pb.redirectOutput(ProcessBuilder.Redirect.INHERIT)
//        pb.redirectError(ProcessBuilder.Redirect.INHERIT)
        pb.redirectErrorStream(true)
        process = pb.start()
    }

    override val pid: Long
        get() = process.pid()
    override val stdin: OutputStream = process.outputStream.wrap()
    override val stdout: InputStream = process.inputStream.wrap()
    override val stderr: InputStream = process.errorStream.wrap()
    override val exitStatus: Int
        get() {
            try {
                return process.exitValue()
            } catch (e: IllegalThreadStateException) {
                throw Process.ProcessStillActive()
            }
        }
    override val isActive: Boolean
        get() {
            val r = process.isAlive
            println("process.isAlive=$r")
            return r
        }

    override fun join() {
        process.waitFor()
    }

    override fun close() {
        process.destroy()
    }

}

actual fun Process.Companion.execute(path: String, args: Array<String>, workDir: String?): Process =
        JvmProcess(cmd = path, args = args.toList(), workDir = workDir)