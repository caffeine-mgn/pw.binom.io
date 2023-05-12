package pw.binom.process

import kotlinx.cinterop.*
import platform.common.internal_get_ERRNO
import platform.posix.*
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime
import kotlin.time.TimeSource

private fun WEXITSTATUS(x: Int) = (x shr 8)

class LinuxProcess(val linuxProcessStarter: LinuxProcessStarter) : Process {
    override var pid: Long = 0

    override val exitStatus: Int
        get() {
            if (internalExitStatus != -1) {
                return internalExitStatus
            }
            if (!isActive) {
                join()
                return internalExitStatus
            }
            throw Process.ProcessStillActive()
//            memScoped {
//                val status = alloc<IntVar>()
//                status.value = -1
//                val r = waitpid(pid.toInt(), status.ptr, WNOHANG)
//                println("waitpid.r=$r")
//                if (isActive) {
//                    throw Process.ProcessStillActive()
//                }
//                return WEXITSTATUS(status.value)
//            }
        }
    override val isActive: Boolean
        get() = internalExitStatus == -1 && waitpid(pid.toInt(), null, WNOHANG) == 0

    private var internalExitStatus = -1

    override fun join() {
        memScoped {
            val status = alloc<IntVar>()
            if (waitpid(pid.toInt(), status.ptr, 0) != -1) {
                internalExitStatus = WEXITSTATUS(status.value)
            }
        }
    }

    override val stdin
        get() = linuxProcessStarter.stdin
    override val stdout
        get() = linuxProcessStarter.stdout
    override val stderr
        get() = linuxProcessStarter.stderr

    init {
        when (val r = fork()) {
            -1 -> throw RuntimeException("Can't start ${linuxProcessStarter.exe}")
            0 -> {
                linuxProcessStarter.env.forEach {
                    setenv(it.key, it.value, 1)
                }

                dup2(stdout.write, STDOUT_FILENO)
                close(stdout.read)
                dup2(stderr.write, STDERR_FILENO)
                close(stderr.read)
                dup2(stdin.read, STDIN_FILENO)
                close(stdin.write)
//                close(errorOnRun.read)

                memScoped {
                    val argsPtr = allocArray<CPointerVar<ByteVar>>(linuxProcessStarter.args.size + 2)
                    argsPtr[0] = linuxProcessStarter.exe.cstr.ptr
                    linuxProcessStarter.args.forEachIndexed { index, s ->
                        argsPtr[index + 1] = s.cstr.ptr
                    }
                    argsPtr[linuxProcessStarter.args.lastIndex + 2] = null
                    if (linuxProcessStarter.workDir != null) {
                        chdir(linuxProcessStarter.workDir)
                    }
//                    byteArrayOf(1).wrap {
//                        errorOnRun.write(it)
//                    }
                    val returnCode = execv(linuxProcessStarter.exe, argsPtr)
                    if (returnCode != 0) {
//                        byteArrayOf(internal_get_ERRNO().toByte()).wrap {
//                            errorOnRun.write(it)
//                        }
                        exitProcess(internal_get_ERRNO())
                        return@memScoped
                    }
                }
            }

            else -> {
                pid = r.toLong()
//                close(errorOnRun.write)
                close(stdout.write)
                close(stderr.write)
                close(stdin.read)

//                val errno = ByteBuffer(1).use {
//                    println("Wait starting")
//                    errorOnRun.read(it)
//                    it.clear()
//                    if (fcntl(errorOnRun.read, F_SETFL, O_NONBLOCK) != 0) {
//                        error("Can't change pipe to non blocking")
//                    }
//                    it.clear()
//                    val readed = errorOnRun.read(it)
//                    println("->readed: $readed")
//                    if (readed == 1) {
//                        it.flip()
//                        it.getByte().toInt()
//                    } else {
//                        0
//                    }
//                }
//                if (errno != 0) {
//                    throw ProcessException("Can't start $exe: ${strerror(errno)?.toKString() ?: "errno #$errno"}")
//                }
            }
        }
    }

    @OptIn(ExperimentalTime::class)
    override fun close() {
        if (!isActive) {
            return
        }
//        errorOnRun.free()
        kill(pid.convert(), SIGINT)
        val now = TimeSource.Monotonic.markNow()
        while (now.elapsedNow().toDouble(DurationUnit.MILLISECONDS) < 1000 && isActive) {
            // NOP
        }
        if (isActive) {
            kill(pid.convert(), SIGKILL)
        }
    }
}
