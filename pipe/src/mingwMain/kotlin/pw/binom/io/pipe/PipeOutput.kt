package pw.binom.io.pipe

import kotlinx.cinterop.*
import platform.windows.*
import pw.binom.io.ByteBuffer
import pw.binom.io.Output

actual class PipeOutput private constructor(fd: Pair<HANDLE?, HANDLE?>) : Output {
    internal var writeFd = fd.first
    internal var readFd = fd.second

    init {
        if (SetHandleInformation(writeFd, HANDLE_FLAG_INHERIT, 0) <= 0) {
            TODO("#4")
        }
    }

    actual constructor() : this(createPipe())

    actual constructor(input: PipeInput) : this(input.writeFd to input.readFd)

    override fun write(data: ByteBuffer): Int {
        if (!data.isReferenceAccessAvailable()) {
            return 0
        }
        memScoped {
            val dwWritten = alloc<UIntVar>()

            val r = data.ref(0) { dataPtr, _ ->
                WriteFile(
                    writeFd,
                    dataPtr.getPointer(this).reinterpret(),
                    data.remaining.convert(),
                    dwWritten.ptr,
                    null,
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
        // Do nonthing
    }

    override fun close() {
        CloseHandle(writeFd)
    }
}
