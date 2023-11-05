package pw.binom.io.socket

import kotlinx.cinterop.*
import platform.posix.pipe

@OptIn(ExperimentalForeignApi::class)
internal actual fun createPipe(): Pair<Int, Int> = memScoped {
  val fds = allocArray<IntVar>(2)
  pipe(fds)
  val pipeRead = fds[0]
  val pipeWrite = fds[1]

  setBlocking(pipeRead, false)
  setBlocking(pipeWrite, false)
  pipeRead to pipeWrite
}

internal actual fun closePipe(value: Int) {
  platform.posix.close(value)
}
