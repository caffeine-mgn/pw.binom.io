package pw.binom.wasm.visitors

interface TableSectionVisitor {
  fun start() {}
  fun end() {}
  fun type(): ValueVisitor.RefVisitor
  fun limit(inital: UInt)
  fun limit(inital: UInt, max: UInt)
}
