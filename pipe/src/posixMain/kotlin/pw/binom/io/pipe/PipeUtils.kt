package pw.binom.io.pipe

import kotlinx.cinterop.IntVar
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.get
import kotlinx.cinterop.memScoped
import platform.posix.pipe

internal fun createPipe() = memScoped {
    val fds = allocArray<IntVar>(2)
    pipe(fds)
    intArrayOf(
        fds[0], // read
        fds[1], // write
    )
}
