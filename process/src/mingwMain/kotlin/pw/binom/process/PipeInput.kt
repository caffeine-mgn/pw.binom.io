package pw.binom.process

import kotlinx.cinterop.*
import platform.windows.*
import pw.binom.io.InputStream

class PipeInput(val process: WinProcess) : Pipe(), InputStream {
    override val handler: HANDLE
        get() = writePipe.pointed.value!!

    override val otherHandler: HANDLE
        get() = readPipe.pointed.value!!

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
                    return -1

                if (totalAvailableBytes.value > 0u)
                    return totalAvailableBytes.value.toInt()

                return if (process.isActive) -1 else 0
            }
        }

    override fun read(data: ByteArray, offset: Int, length: Int): Int {
        if (data.size < offset + length)
            throw IndexOutOfBoundsException()
        memScoped {

            val dwWritten = alloc<UIntVar>()
            println("Try to read $length  $available")

            if (available == 0)
                return 0

//            val r = ReadConsoleInput!!.invoke(readPipe.pointed.value, data.refTo(offset).getPointer(this).reinterpret(), length.convert(), dwWritten.ptr)
            val r = ReadFile(readPipe.pointed.value, data.refTo(offset).getPointer(this).reinterpret(),
                    length.convert(), dwWritten.ptr, null)
            println("Readed ${dwWritten.value} <----> $r")
            if (r <= 0)
                TODO()

            return dwWritten.value.toInt()
        }
    }

    init {
//        if (SetHandleInformation(readPipe.pointed.value, HANDLE_FLAG_INHERIT, 0) <= 0)
//            TODO("#4")
    }
}