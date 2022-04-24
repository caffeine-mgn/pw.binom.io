package pw.binom.process

import kotlinx.cinterop.*
import platform.windows.*
import pw.binom.ByteBuffer
import pw.binom.Input
import pw.binom.concurrency.sleep

class PipeInput(val process: WinProcess) : Pipe(), Input {
    override val handler: HANDLE
        get() = writePipe.pointed.value!!

    override val otherHandler: HANDLE
        get() = readPipe.pointed.value!! // rr.pointed.value!!

    val available: Int
        get() {
            memScoped {
                val totalAvailableBytes = alloc<UIntVar>()
                if (PeekNamedPipe(
                        readPipe.pointed.value,
                        null,
                        0.convert(),
                        null,
                        totalAvailableBytes.ptr,
                        null
                    ) == 0
                )
                    return -2

                if (totalAvailableBytes.value > 0u)
                    return totalAvailableBytes.value.toInt()

                return if (process.isActive) -1 else 0
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

    override fun read(dest: ByteBuffer): Int {
        if (!dest.isReferenceAccessAvailable()) {
            return 0
        }
        while (true) {
            sleep(1)
            if (available == 0) {
                return 0
            }

            if (available > 0)
                break
        }

        memScoped {

            val dwWritten = alloc<UIntVar>()

            val r = dest.refTo(dest.position) { destPtr ->
                ReadFile(
                    otherHandler, (destPtr).getPointer(this).reinterpret(),
                    dest.remaining.convert(), dwWritten.ptr, null
                )
            } ?: 0
            if (r <= 0)
                TODO()
            val read = dwWritten.value.toInt()
            dest.position += read
            return read
        }
    }
}
