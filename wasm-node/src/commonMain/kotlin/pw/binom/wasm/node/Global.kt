package pw.binom.wasm.node

data class Global(
    val type: ValueType,
    val mutable: Boolean,
    val expressions: Expressions,
)
