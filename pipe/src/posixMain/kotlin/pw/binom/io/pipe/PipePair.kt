package pw.binom.io.pipe

import kotlinx.cinterop.*
import platform.common.internal_pipe2
import platform.posix.*
import pw.binom.io.IOException

value class PipePair private constructor(private val raw: IntArray) {

  companion object {
    @OptIn(ExperimentalForeignApi::class)
    private fun createPipe() = memScoped {
      val fds = allocArray<IntVar>(2)
//      if (pipe(fds) != 0) {
      if (internal_pipe2(fds, O_CLOEXEC or O_NONBLOCK) != 0) {
        throw IOException("Can't create pipe. Errno: $errno")
      }

      intArrayOf(
        fds[0], // read
        fds[1], // write
      )
    }
  }

  constructor() : this(createPipe())
  constructor(readFd: Int, writeFd: Int) : this(intArrayOf(readFd, writeFd))

  val readFd
    get() = raw[0]
  val writeFd
    get() = raw[1]
}
