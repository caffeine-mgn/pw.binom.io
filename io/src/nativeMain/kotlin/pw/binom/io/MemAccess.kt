package pw.binom.io

import kotlinx.cinterop.*
import pw.binom.memory.Memory

@ExperimentalForeignApi
sealed interface MemAccess : Closeable {
  val capacity: Int

  val pointer: CArrayPointer<ByteVar>
  fun <T> access(func: (CPointer<ByteVar>) -> T): T
  fun <T> access2(func: (COpaquePointer) -> T): T

  class NativeMemory(val ptr: COpaquePointer, override val capacity: Int) : MemAccess {
    private var closed = false

    constructor(size: Int) : this(
      ptr = Memory.alloc(size.convert()),
      capacity = size,
    )

    override val pointer: CPointer<ByteVar> = ptr.reinterpret()

    override fun <T> access(func: (CPointer<ByteVar>) -> T): T {
      if (closed) {
        throw ClosedException()
      }
      return func(ptr.reinterpret())
    }

    override fun <T> access2(func: (COpaquePointer) -> T): T {
      if (closed) {
        throw ClosedException()
      }
      return func(ptr)
    }

    override fun close() {
      if (closed) {
        return
      }
      closed = true
      Memory.free(ptr)
    }
  }

  class HeapMemory(val ptr: CArrayPointer<ByteVar>, override val capacity: Int) : MemAccess {
    constructor(size: Int) : this(ptr = nativeHeap.allocArray<ByteVar>(size), capacity = size)

    override val pointer: CPointer<ByteVar> = ptr

    override fun <T> access(func: (CPointer<ByteVar>) -> T): T = func(ptr)
    override fun <T> access2(func: (COpaquePointer) -> T): T = func(ptr)

    override fun close() {
      nativeHeap.free(ptr)
    }
  }

  class ArrayMemory(array: ByteArray) : MemAccess {
    override val capacity: Int = array.size
    private val pin = array.pin()
    override val pointer: CPointer<ByteVar> = pin.addressOf(0)
    override fun <T> access(func: (CPointer<ByteVar>) -> T): T = func(pin.addressOf(0))
    override fun <T> access2(func: (COpaquePointer) -> T): T = func(pin.addressOf(0))

    override fun close() {
      pin.unpin()
    }
  }

  object EmptyMemory : MemAccess {
    override val capacity: Int = 0
    override val pointer: CPointer<ByteVar> = 1L.toCPointer()!!
    override fun <T> access(func: (CPointer<ByteVar>) -> T): T = TODO()
    override fun <T> access2(func: (COpaquePointer) -> T): T = TODO()

    override fun close() {
    }
  }
}
