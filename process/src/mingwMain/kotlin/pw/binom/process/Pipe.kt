package pw.binom.process

import kotlinx.cinterop.*
import platform.posix.free
import platform.posix.memset
import platform.windows.*
import pw.binom.io.Closeable

abstract class Pipe : Closeable {

    val readPipe = platform.posix.malloc(sizeOf<HANDLEVar>().convert())!!.reinterpret<HANDLEVar>()
    val writePipe = platform.posix.malloc(sizeOf<HANDLEVar>().convert())!!.reinterpret<HANDLEVar>()

    abstract val handler: HANDLE
    abstract val otherHandler: HANDLE

    init {
        memScoped {
            val saAttr = alloc<SECURITY_ATTRIBUTES>()
            memset(saAttr.ptr, 0, sizeOf<SECURITY_ATTRIBUTES>().convert())

            saAttr.nLength = sizeOf<SECURITY_ATTRIBUTES>().convert()
            saAttr.bInheritHandle = TRUE
            saAttr.lpSecurityDescriptor = null

            if (CreatePipe(readPipe, writePipe, saAttr.ptr, 0) <= 0)
                TODO("#3")
        }
    }

    override fun close() {
        CloseHandle(readPipe.pointed.value)
        CloseHandle(writePipe.pointed.value)

        free(readPipe)
        free(writePipe)
    }
}
