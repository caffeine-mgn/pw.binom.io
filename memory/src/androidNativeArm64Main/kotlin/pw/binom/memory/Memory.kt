package pw.binom.memory

import kotlinx.cinterop.*
import platform.posix.malloc
import platform.posix.memcpy

@OptIn(ExperimentalForeignApi::class)
actual object Memory {
  actual fun alloc(size: Long): COpaquePointer =
    malloc(size.convert()) ?: throw IllegalStateException("Can't alloc $size bytes")

  actual fun free(ptr: COpaquePointer) {
    platform.posix.free(ptr)
  }
}

@OptIn(ExperimentalForeignApi::class)
actual fun CPointer<ByteVar>.copyInto(dest: CPointer<ByteVar>, size: Long) {
  memcpy(dest, this, size.convert())
}

@OptIn(ExperimentalForeignApi::class)
actual fun COpaquePointer.copyInto(dest: COpaquePointer, size: Long) {
  memcpy(dest, this, size.convert())
}