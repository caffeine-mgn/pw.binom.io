package pw.binom.wasm

interface ExpressionsVisitor {
  companion object {
    val STUB = object : ExpressionsVisitor {}
  }

  fun start() {}
  fun end() {}
}
