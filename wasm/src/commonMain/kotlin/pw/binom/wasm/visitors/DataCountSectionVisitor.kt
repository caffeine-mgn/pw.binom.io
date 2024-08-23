package pw.binom.wasm.visitors

interface DataCountSectionVisitor {
  fun start()
  fun end()
  fun value(count: UInt)
}
