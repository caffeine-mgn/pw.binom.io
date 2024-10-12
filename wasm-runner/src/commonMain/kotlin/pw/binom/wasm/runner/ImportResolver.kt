package pw.binom.wasm.runner

import pw.binom.wasm.node.ValueType

interface ImportResolver {
  fun global(module: String, field: String, type: ValueType, mutable: Boolean): GlobalVar? = null
  fun memory(module: String, field: String, inital: UInt, max: UInt?): MemorySpace? = null
  fun func(module: String, field: String): ((ExecuteContext) -> Unit)? = null
}

interface ExecuteContext {
  val runner: Runner
  val args: List<Any>
  fun pushResult(value: Any)
  fun stop()
}
