package pw.binom.wasm.visitors

interface ExportSectionVisitor {
  companion object {
    val SKIP = object : ExportSectionVisitor {}
  }

  fun start() {}
  fun end() {}
  fun func(name: String, value: UInt) {}
  fun table(name: String, value: UInt) {}
  fun memory(name: String, value: UInt) {}
  fun global(name: String, value: UInt) {}
}
