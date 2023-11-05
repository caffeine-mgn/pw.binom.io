package pw.binom.io.pipe

import kotlinx.cinterop.*
import platform.posix.pipe

@OptIn(ExperimentalForeignApi::class)
internal fun createPipe() = memScoped {
  val fds = allocArray<IntVar>(2)
  pipe(fds)
  intArrayOf(
    fds[0], // read
    fds[1], // write
  )
}
