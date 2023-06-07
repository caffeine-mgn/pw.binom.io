package pw.binom.io

import kotlinx.cinterop.*
import platform.posix.memset

expect fun CPointer<ByteVar>.copyInto(dest: CPointer<ByteVar>, size: Long)
expect fun COpaquePointer.copyInto(dest: COpaquePointer, size: Long)

expect object Memory {
    fun alloc(size: Long): COpaquePointer
    fun free(ptr: COpaquePointer)
}

value class InHeap<T : CVariable>(val raw: ByteArray) {
    companion object {
        fun <T : CVariable> create(size: Int) = InHeap<T>(ByteArray(size))
        inline fun <reified T : CVariable> create() = InHeap<T>(ByteArray(sizeOf<T>().toInt()))
    }

    fun clear() {
        raw.usePinned { pinned ->
            memset(pinned.addressOf(0), raw.size, 1)
        }
    }

    fun copy() = InHeap<T>(raw.copyOf())

    @Suppress("NOTHING_TO_INLINE")
    inline fun copyInto(other: InHeap<T>) = raw.copyInto(other.raw)

    inline fun <R> use(func: (CPointer<T>) -> R) =
        raw.usePinned {
            func(it.addressOf(0).reinterpret())
        }
}
