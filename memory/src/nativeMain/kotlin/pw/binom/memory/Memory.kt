package pw.binom.memory

import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi

@OptIn(ExperimentalForeignApi::class)
expect object Memory {
  fun alloc(size: Long): COpaquePointer
  fun free(ptr: COpaquePointer)
}

@OptIn(ExperimentalForeignApi::class)
expect fun CPointer<ByteVar>.copyInto(dest: CPointer<ByteVar>, size: Long)

@OptIn(ExperimentalForeignApi::class)
expect fun COpaquePointer.copyInto(dest: COpaquePointer, size: Long)
