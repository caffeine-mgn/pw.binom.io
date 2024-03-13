@file:OptIn(ExperimentalForeignApi::class)

package pw.binom.io

import kotlinx.cinterop.*
import pw.binom.memory.zero

@OptIn(ExperimentalForeignApi::class)
value class InHeap<T : CVariable>(val raw: ByteArray) {
  companion object {
    fun <T : CVariable> create(size: Int) = InHeap<T>(ByteArray(size))
    inline fun <reified T : CVariable> create() = InHeap<T>(ByteArray(sizeOf<T>().toInt()))
  }

  fun clear() {
    raw.zero()
  }

  fun copy() = InHeap<T>(raw.copyOf())

  @Suppress("NOTHING_TO_INLINE")
  inline fun copyInto(other: InHeap<T>) = raw.copyInto(other.raw)

  inline fun <R> use(func: (CPointer<T>) -> R) =
    raw.usePinned {
      func(it.addressOf(0).reinterpret())
    }
}
