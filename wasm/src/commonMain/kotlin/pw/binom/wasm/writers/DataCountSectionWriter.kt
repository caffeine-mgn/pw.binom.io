package pw.binom.wasm.writers

import pw.binom.wasm.WasmOutput
import pw.binom.wasm.visitors.DataCountSectionVisitor

class DataCountSectionWriter(private val out: WasmOutput) : DataCountSectionVisitor {
  private var state = 0
  override fun start() {
    check(state == 0)
    state++
  }

  override fun end() {
    check(state == 2)
    state = 0
  }

  override fun value(count: UInt) {
    check(state == 1)
    state++
    out.v32u(count)
  }
}
