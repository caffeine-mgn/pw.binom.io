package pw.binom.wasm.wasi

import kotlin.wasm.unsafe.Pointer
import kotlin.wasm.unsafe.UnsafeWasmMemoryApi

@OptIn(UnsafeWasmMemoryApi::class)
fun Pointer.storeUInt(value: UInt) {
  storeInt(value.toInt())
}

@OptIn(UnsafeWasmMemoryApi::class)
fun Pointer.loadUInt() = loadInt().toUInt()

@OptIn(UnsafeWasmMemoryApi::class)
fun Pointer.storeULong(value: ULong) {
  storeLong(value.toLong())
}

@OptIn(UnsafeWasmMemoryApi::class)
fun Pointer.loadULong() = loadLong().toULong()

@OptIn(UnsafeWasmMemoryApi::class)
fun Pointer.storeUShort(value: UShort) {
  storeShort(value.toShort())
}

@OptIn(UnsafeWasmMemoryApi::class)
fun Pointer.loadUShort() = loadShort().toUShort()

@OptIn(UnsafeWasmMemoryApi::class)
fun Pointer.storeUByte(value: UByte) {
  storeByte(value.toByte())
}

@OptIn(UnsafeWasmMemoryApi::class)
fun Pointer.loadUByte() = loadByte().toUByte()
