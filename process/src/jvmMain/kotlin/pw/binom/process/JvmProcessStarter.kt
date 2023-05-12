package pw.binom.process

import pw.binom.io.ByteBuffer
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
        override fun write(data: ByteBuffer): Int {
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
        override fun read(dest: ByteBuffer): Int {
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
        override fun read(dest: ByteBuffer): Int {
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
        val pb = ProcessBuilder(listOf(path) + args)
        pb.environment().clear()
        pb.environment().putAll(envs)
        workDir?.let { pb.directory(File(it)) }
        pb.redirectOutput(ProcessBuilder.Redirect.PIPE)
        pb.redirectError(ProcessBuilder.Redirect.PIPE)
        return JvmProcess(process = pb.start(), processStarter = this)
    }
}
