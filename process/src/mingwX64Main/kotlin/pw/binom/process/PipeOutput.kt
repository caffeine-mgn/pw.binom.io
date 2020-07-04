package pw.binom.process

import kotlinx.cinterop.*
import platform.windows.HANDLE
import platform.windows.HANDLE_FLAG_INHERIT
import platform.windows.SetHandleInformation
import platform.windows.WriteFile
import pw.binom.Output
import kotlin.native.concurrent.freeze

class PipeOutput : Pipe(), Output {
    override val handler: HANDLE
        get() = readPipe.pointed.value!!

    override val otherHandler: HANDLE
        get() = writePipe.pointed.value!!

    init {
        if (SetHandleInformation(writePipe.pointed.value, HANDLE_FLAG_INHERIT, 0) <= 0)
            TODO("#4")
    }

    override fun write(data: ByteArray, offset: Int, length: Int): Int {
        if (data.size < offset + length)
            throw IndexOutOfBoundsException()
        memScoped {
            val dwWritten = alloc<UIntVar>()
            val r = WriteFile(writePipe.pointed.value, data.refTo(offset).getPointer(this).reinterpret(),
                    length.convert(), dwWritten.ptr, null)
            if (r <= 0)
                TODO()
            return dwWritten.value.toInt()
        }
    }

    override fun flush() {
    }
}