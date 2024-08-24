package pw.binom.wasm.writers

import pw.binom.copyTo
import pw.binom.io.Input
import pw.binom.wasm.WasmOutput
import pw.binom.wasm.visitors.CustomSectionVisitor

class CustomSectionWriter(private val out: WasmOutput) : CustomSectionVisitor {

  private var state = 0

  override fun start(name: String) {
    check(state == 0)
    state++
    out.string(name)
  }

  override fun end() {
    check(state == 2)
    state = 0
  }

  override fun data(input: Input) {
    check(state == 1)
    state++
    input.copyTo(out)
  }
}
