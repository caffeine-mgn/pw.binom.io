package pw.binom.wasm

import pw.binom.io.Input

interface CustomSectionVisitor {
  fun start(name: String)
  fun data(input: Input)
  fun end()
}
