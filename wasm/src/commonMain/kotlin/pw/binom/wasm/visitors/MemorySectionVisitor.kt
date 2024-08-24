package pw.binom.wasm.visitors

interface MemorySectionVisitor {
  companion object {
    val SKIP = object : MemorySectionVisitor {}
  }

  fun start() {}
  fun memory(inital: UInt) {}
  fun memory(inital: UInt, max: UInt) {}
  fun end() {}
}
