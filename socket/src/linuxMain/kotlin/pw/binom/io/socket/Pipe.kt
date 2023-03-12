package pw.binom.io.socket

import kotlinx.cinterop.IntVar
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.get
import kotlinx.cinterop.memScoped
import platform.posix.pipe

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
