package pw.binom.wasm.runner

import pw.binom.wasm.node.ValueType

interface ImportResolver {
  fun global(module: String, field: String, type: ValueType, mutable: Boolean): GlobalVar
  fun memory(module: String, field: String, inital: UInt, max: UInt?): MemorySpace
}
