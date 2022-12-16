package pw.binom.io

import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.CPointer

expect fun CPointer<ByteVar>.copy(dest: CPointer<ByteVar>, size: Long)
expect fun COpaquePointer.copy(dest: COpaquePointer, size: Long)

expect object Memory {
    fun alloc(size: Long): COpaquePointer
    fun free(ptr: COpaquePointer)
}
