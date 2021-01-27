package pw.binom.process

import kotlinx.cinterop.*
import platform.windows.HANDLE
import platform.windows.HANDLE_FLAG_INHERIT
import platform.windows.SetHandleInformation
import platform.windows.WriteFile
import pw.binom.ByteBuffer
import pw.binom.Output

class PipeOutput : Pipe(), Output {
    override val handler: HANDLE
        get() = readPipe.pointed.value!!

    override val otherHandler: HANDLE
        get() = writePipe.pointed.value!!

    init {
        if (SetHandleInformation(writePipe.pointed.value, HANDLE_FLAG_INHERIT, 0) <= 0)
            TODO("#4")
    }

    override fun write(data: ByteBuffer): Int {
        memScoped {
            val dwWritten = alloc<UIntVar>()

            val r = WriteFile(writePipe.pointed.value, (data.refTo(data.position)).getPointer(this).reinterpret(),
                    data.remaining.convert(), dwWritten.ptr, null)
            if (r <= 0)
                TODO()
            val wrote = dwWritten.value.toInt()
            data.position += wrote
            return wrote
        }
    }

    override fun flush() {
    }
}