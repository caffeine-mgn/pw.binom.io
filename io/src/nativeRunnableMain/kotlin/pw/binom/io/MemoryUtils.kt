package pw.binom.io

import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.convert
import platform.posix.malloc
import platform.posix.memcpy
import platform.posix.free as nativeFree

actual fun CPointer<ByteVar>.copyInto(dest: CPointer<ByteVar>, size: Long) {
    memcpy(dest, this, size.convert())
}

actual fun COpaquePointer.copyInto(dest: COpaquePointer, size: Long) {
    memcpy(dest, this, size.convert())
}

actual object Memory {
    actual fun alloc(size: Long): COpaquePointer =
        malloc(size.convert()) ?: throw IllegalStateException("Can't alloc $size bytes")

    actual fun free(ptr: COpaquePointer) {
        nativeFree(ptr)
    }
}
