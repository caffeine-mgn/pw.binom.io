package pw.binom.wasm.wasi

import kotlin.wasm.unsafe.MemoryAllocator
import kotlin.wasm.unsafe.Pointer
import kotlin.wasm.unsafe.UnsafeWasmMemoryApi

@OptIn(UnsafeWasmMemoryApi::class)
fun MemoryAllocator.load(address: Pointer, data: ByteArray) {
  var p = address
  repeat(data.size) { i ->
    data[i] = p.loadByte()
    p += 1
  }
}

@OptIn(UnsafeWasmMemoryApi::class)
fun MemoryAllocator.loadBytes(address: Pointer, size: Int) =
  ByteArray(size) { index ->
    (address + index).loadByte()
  }

@OptIn(UnsafeWasmMemoryApi::class)
fun MemoryAllocator.store(address: Pointer, data: ByteArray) {
  var p = address
  data.forEach {
    p.storeByte(it)
    p += 1
  }
}

@OptIn(UnsafeWasmMemoryApi::class)
fun MemoryAllocator.store(data: ByteArray): Pointer {
  val pointer = allocate(data.size)
  store(pointer, data)
  return pointer
}
