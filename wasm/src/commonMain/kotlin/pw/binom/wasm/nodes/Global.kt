package pw.binom.wasm.nodes

data class Global(
  val type: ValueType,
  val mutable: Boolean,
  val expressions: Expressions,
)
