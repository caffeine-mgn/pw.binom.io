package pw.binom.io

import kotlinx.cinterop.*

actual fun CPointer<ByteVar>.copyInto(dest: CPointer<ByteVar>, size: Long) {
    var c = 0L
    while (c < size) {
        dest[c] = get(c)
        c++
    }
}

actual fun COpaquePointer.copyInto(dest: COpaquePointer, size: Long) {
    val self = this.reinterpret<ByteVar>()
    val dest2 = dest.reinterpret<ByteVar>()
    var c = 0L
    while (c < size) {
        dest2[c] = self[c]
        c++
    }
}

actual object Memory {
    actual fun alloc(size: Long): COpaquePointer = nativeHeap.allocArray<ByteVar>(size)
    actual fun free(ptr: COpaquePointer) {
        nativeHeap.free(ptr)
    }
}
