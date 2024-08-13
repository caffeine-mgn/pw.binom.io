package pw.binom.wasm

interface ImportSectionVisitor {
  fun start()
  fun end()
  fun function(module: String, field: String, index: UInt)
  fun memory(module: String, field: String, initial: UInt, maximum: UInt)
  fun memory(module: String, field: String, initial: UInt)
  fun table(module: String, field: String, type: ValueType.Ref, min: UInt, max: UInt) {}
  fun table(module: String, field: String, type: ValueType.Ref, min: UInt) {}
}
