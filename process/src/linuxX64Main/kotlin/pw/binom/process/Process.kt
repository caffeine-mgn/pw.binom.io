package pw.binom.process

import kotlinx.cinterop.*
import platform.posix.*

private fun WEXITSTATUS(x: Int) = (x shr 8)

class LinuxProcess(exe: String, args: List<String>, workDir: String?) : Process {
    override var pid: Long = 0

    override val exitStatus: Int
        get() {
            memScoped {
                val status = alloc<IntVar>()
                status.value = -1
                val r = waitpid(pid.toInt(), status.ptr, WNOHANG)
                if (r != pid.toInt())
                    throw Process.ProcessStillActive()
                return WEXITSTATUS(status.value)
            }
        }
    override val isActive: Boolean
        get() = waitpid(pid.toInt(), null, WNOHANG) == 0

    override fun join() {
        memScoped {
            val status = alloc<IntVar>()
            waitpid(pid.toInt(), status.ptr, 0)
        }
    }

    override val stdin = PipeOutput()
    override val stdout = PipeInput()
    override val stderr = PipeInput()

    init {
        when (val r = fork()) {
            -1 -> throw RuntimeException("Can't start $exe")
            0 -> {
                println("stdout.write=${stdout.write}")
                println("stdout.read=${stdout.read}")
                val bb = dup2(stdout.write, STDOUT_FILENO)
                if (bb == -1)
                    TODO("dup2= $bb error $errno    STDOUT_FILENO=$STDOUT_FILENO")
                close(stdout.read)
                dup2(stderr.write, STDERR_FILENO)
                close(stderr.read)
                dup2(stdin.read, STDIN_FILENO)
                close(stdin.write)

                memScoped {
                    val r = allocArray<CPointerVar<ByteVar>>(args.size + 2)
                    r[0] = exe.cstr.ptr
                    args.forEachIndexed { index, s ->
                        r[index + 1] = s.cstr.ptr
                    }
                    r[args.lastIndex + 2] = null
                    println("Run $exe with args: [${args.joinToString(",")}]")
                    if (workDir != null)
                        chdir(workDir)
                    val rr = execv(exe, r)
                    println("Result: $rr $errno")
                    TODO()
                }

            }
            else -> {
                pid = r.toLong()
                close(stdout.write)
                close(stderr.write)
                close(stdin.read)
                println("Process started. pid=$pid")
            }
        }
    }

    override fun close() {
        stdout.free()
        stderr.free()
        stdin.free()
    }

}

actual fun Process.Companion.execute(path: String, args: Array<String>, workDir: String?): Process =
        LinuxProcess(exe = path, args = args.toList(), workDir = workDir)