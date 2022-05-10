package pw.binom.process

import kotlinx.cinterop.*
import platform.windows.HANDLE
import platform.windows.HANDLE_FLAG_INHERIT
import platform.windows.SetHandleInformation
import platform.windows.WriteFile
import pw.binom.io.ByteBuffer
import pw.binom.io.Output

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
        if (!data.isReferenceAccessAvailable()) {
            return 0
        }
        memScoped {
            val dwWritten = alloc<UIntVar>()

            val r = data.ref { dataPtr, _ ->
                WriteFile(
                    writePipe.pointed.value, dataPtr.getPointer(this).reinterpret(),
                    data.remaining123.convert(), dwWritten.ptr, null
                )
            } ?: 0
            if (r <= 0) {
                TODO("WriteFile returned $r")
            }
            val wrote = dwWritten.value.toInt()
            data.position += wrote
            return wrote
        }
    }

    override fun flush() {
    }
}
