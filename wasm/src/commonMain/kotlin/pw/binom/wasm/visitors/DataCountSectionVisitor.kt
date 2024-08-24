package pw.binom.wasm.visitors

interface DataCountSectionVisitor {
  companion object {
    val SKIP = object : DataCountSectionVisitor {}
  }

  fun start() {}
  fun end() {}
  fun value(count: UInt) {}
}
