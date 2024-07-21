package pw.binom.process

import kotlinx.cinterop.*
import platform.common.internal_get_ERRNO
import platform.posix.*
import pw.binom.concurrency.synchronize
import kotlin.time.DurationUnit
import kotlin.time.TimeSource

@OptIn(ExperimentalForeignApi::class)
class PosixProcess internal constructor(private val posixProcessStarter: PosixProcessStarter) : Process {
  override var pid: Long = 0
    private set

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
    }
  override val isActive: Boolean
    get() = internalExitStatus == -1 && waitpid(pid.toInt(), null, WNOHANG) == 0

  private var internalExitStatus = -1

  override val stdin
    get() = posixProcessStarter.stdin
  override val stdout
    get() = posixProcessStarter.stdout
  override val stderr
    get() = posixProcessStarter.stderr

  override fun join() {
    memScoped {
      val status = alloc<IntVar>()
      if (waitpid(pid.toInt(), status.ptr, 0) != -1) {
        internalExitStatus = status.value shr 8
      }
    }
  }

  init {
    when (val r = fork()) {
      -1 -> throw RuntimeException("Can't start ${posixProcessStarter.exe}")
      0 -> {
        posixProcessStarter.env.forEach {
          setenv(it.key, it.value, 1)
        }
        dup2(posixProcessStarter.io.stdout.writeFd, STDOUT_FILENO)
        close(posixProcessStarter.io.stdout.readFd)
        dup2(posixProcessStarter.io.stderr.writeFd, STDERR_FILENO)
        close(posixProcessStarter.io.stderr.readFd)
        dup2(posixProcessStarter.io.stdin.readFd, STDIN_FILENO)
        close(posixProcessStarter.io.stdin.writeFd)

        memScoped {
          val argsPtr = allocArray<CPointerVar<ByteVar>>(posixProcessStarter.args.size + 2)
          argsPtr[0] = posixProcessStarter.exe.cstr.ptr
          posixProcessStarter.args.forEachIndexed { index, s ->
            argsPtr[index + 1] = s.cstr.ptr
          }
          argsPtr[posixProcessStarter.args.lastIndex + 2] = null
          if (posixProcessStarter.workDir != null) {
            chdir(posixProcessStarter.workDir)
          }
//          println("Run process")
          val returnCode = execv(posixProcessStarter.exe, argsPtr)
          close(posixProcessStarter.io.stdout.writeFd)
          close(posixProcessStarter.io.stderr.writeFd)
          close(posixProcessStarter.io.stdin.readFd)
          if (returnCode != 0) {
            exitProcess(internal_get_ERRNO())
            return@memScoped
          } else {
            exitProcess(0)
          }
        }
      }

      else -> {
        pid = r.toLong()
        close(posixProcessStarter.io.stdout.writeFd)
        close(posixProcessStarter.io.stderr.writeFd)
        close(posixProcessStarter.io.stdin.readFd)
        posixProcessStarter.lock.synchronize {
          posixProcessStarter.con.signalAll()
        }
      }
    }
  }

  override fun close() {
    if (!isActive) {
      return
    }
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
