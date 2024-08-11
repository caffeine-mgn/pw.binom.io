package pw.binom.wasm

interface ImportSectionVisitor {
  fun start()
  fun end()
  fun function(module: String, field: String, index: Int)
  fun memory(module: String, field: String, initial: Int, maximum: Int?)
  fun table(module: String, field: String, type: ValueType.Ref, min: Int, max: Int?)
}
