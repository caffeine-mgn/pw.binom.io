package pw.binom.process

import kotlinx.cinterop.*
import platform.windows.*
import pw.binom.Thread
import pw.binom.io.InputStream

class PipeInput(val process: WinProcess) : Pipe(), InputStream {
    override val handler: HANDLE
        get() = writePipe.pointed.value!!

//    val rr = platform.posix.malloc(sizeOf<HANDLEVar>().convert())!!.reinterpret<HANDLEVar>()

    override val otherHandler: HANDLE
        get() = readPipe.pointed.value!!//rr.pointed.value!!


    override val available: Int
        get() {
            memScoped {
                val totalAvailableBytes = alloc<UIntVar>()
                if (PeekNamedPipe(
                                readPipe.pointed.value,
                                null,
                                0.convert(),
                                null,
                                totalAvailableBytes.ptr,
                                null) == 0)
                    return -2

                if (totalAvailableBytes.value > 0u)
                    return totalAvailableBytes.value.toInt()

                return if (process.isActive) -1 else 0
            }
        }

    override fun read(data: ByteArray, offset: Int, length: Int): Int {
        if (data.size < offset + length)
            throw IndexOutOfBoundsException()

        while (true) {
            Thread.sleep(1)
            if (available == 0) {
                return 0
            }

            if (available > 0)
                break
        }

        memScoped {

            val dwWritten = alloc<UIntVar>()


            val r = ReadFile(otherHandler, data.refTo(offset).getPointer(this).reinterpret(),
                    length.convert(), dwWritten.ptr, null)
            if (r <= 0)
                TODO()

            return dwWritten.value.toInt()
        }
    }

    init {

//        DuplicateHandle(GetCurrentProcess(), readPipe.pointed.value,
//                GetCurrentProcess(),
//                rr, // Address of new handle.
//                0, FALSE, // Make it uninheritable.
//                DUPLICATE_SAME_ACCESS)

//        CloseHandle(readPipe.pointed.value)

        if (SetHandleInformation(readPipe.pointed.value, HANDLE_FLAG_INHERIT, 0) <= 0)
            TODO("#4")
    }
}