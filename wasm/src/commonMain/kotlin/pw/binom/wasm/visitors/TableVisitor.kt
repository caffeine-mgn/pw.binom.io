package pw.binom.wasm.visitors

interface TableVisitor {
  companion object {
    val SKIP = object : TableVisitor {

    }
  }

  fun start() {}
  fun range(min: UInt, max: UInt) {}
  fun range(min: UInt) {}
  fun type(): ValueVisitor.RefVisitor = ValueVisitor.RefVisitor.SKIP
  fun end() {}
}
