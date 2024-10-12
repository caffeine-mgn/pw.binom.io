package pw.binom.wasm.runner

import pw.binom.wasm.Primitive
import pw.binom.wasm.node.ValueType

class CLangEnv:ImportResolver {
  override fun global(module: String, field: String, type: ValueType, mutable: Boolean): GlobalVar =
    when {
      module == "env" && field == "__stack_pointer" && type.number?.type == Primitive.I32 ->
        GlobalVarMutable.S32(0)
      else -> TODO()
    }

  override fun memory(module: String, field: String, inital: UInt, max: UInt?): MemorySpace =
    when {
      module == "env" && (field == "__linear_memory" || field == "memory") -> MemorySpaceByteBuffer(1024 * 1024)
      else -> TODO("Not yet implemented. module=$module, field=$field, inital: $inital, max: $max")
    }
}
