package pw.binom.wasm.dom

data class Global(
  val type: ValueType,
  val mutable: Boolean,
  val expressions: Expressions,
)
