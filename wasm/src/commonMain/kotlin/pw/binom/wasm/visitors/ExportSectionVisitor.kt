package pw.binom.wasm.visitors

interface ExportSectionVisitor {
  fun start(name: String) {}
  fun end() {}
  fun func(value: UInt) {}
  fun table(value: UInt) {}
  fun memory(value: UInt) {}
  fun global(value: UInt) {}
}
