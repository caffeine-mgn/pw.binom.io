package pw.binom.process

import kotlinx.cinterop.*
import platform.posix.malloc
import platform.posix.pipe

abstract class Pipe {
    private val fds = malloc((sizeOf<IntVar>() * 2).convert())!!.reinterpret<IntVar>()!!
    val read: Int
        get() = fds[0]

    val write: Int
        get() = fds[1]

    init {
        pipe(fds)
    }

    fun free() {
        platform.posix.free(fds)
    }
}
