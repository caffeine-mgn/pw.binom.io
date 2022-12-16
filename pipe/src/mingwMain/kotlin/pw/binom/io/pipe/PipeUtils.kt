package pw.binom.io.pipe

import kotlinx.cinterop.*
import platform.posix.memset
import platform.windows.*

internal fun createPipe(): Pair<HANDLE?, HANDLE?> = memScoped {
    val saAttr = alloc<SECURITY_ATTRIBUTES>()
    memset(saAttr.ptr, 0, sizeOf<SECURITY_ATTRIBUTES>().convert())

//    val readPipe = platform.posix.malloc(sizeOf<HANDLEVar>().convert())!!.reinterpret<HANDLEVar>()
//    val writePipe = platform.posix.malloc(sizeOf<HANDLEVar>().convert())!!.reinterpret<HANDLEVar>()
    val readPipe = alloc<HANDLEVar>()
    val writePipe = alloc<HANDLEVar>()

    saAttr.nLength = sizeOf<SECURITY_ATTRIBUTES>().convert()
    saAttr.bInheritHandle = TRUE
    saAttr.lpSecurityDescriptor = null
    if (CreatePipe(readPipe.ptr, writePipe.ptr, saAttr.ptr, 0) <= 0) {
        TODO("#3")
    }
    readPipe.value to writePipe.value
}
