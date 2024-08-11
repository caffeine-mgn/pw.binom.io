package pw.binom.wasm

interface TypeSectionVisitor {
  fun start()
  fun argument(type:ValueType)
  fun result(type:ValueType)
  fun end()
}
