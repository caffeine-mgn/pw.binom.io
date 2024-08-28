package pw.binom.wasm.visitors

import pw.binom.io.Input

interface CustomSectionVisitor {
  companion object {
    val SKIP = object : CustomSectionVisitor {}
  }

  fun start(name: String) {}
  fun data(input: Input) {}
  fun end() {}
}
