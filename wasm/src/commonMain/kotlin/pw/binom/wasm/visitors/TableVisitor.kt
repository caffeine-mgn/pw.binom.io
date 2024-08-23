package pw.binom.wasm.visitors

interface TableVisitor {
  companion object {
    val EMPTY = object : TableVisitor {

    }
  }

  fun start() {}
  fun range(min: UInt, max: UInt) {}
  fun range(min: UInt) {}
  fun type(): ValueVisitor.RefVisitor = ValueVisitor.RefVisitor.EMPTY
  fun end() {}
}
