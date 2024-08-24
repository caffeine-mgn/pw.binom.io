package pw.binom.wasm.writers

import pw.binom.wasm.FunctionId
import pw.binom.wasm.WasmOutput
import pw.binom.wasm.visitors.FunctionSectionVisitor

class FunctionSectionWriter(private val out: WasmOutput) : FunctionSectionVisitor {
  private var state = 0

  override fun start() {
    check(state == 0)
    state++
    super.start()
  }

  override fun end() {
    check(state == 2)
    state = 0
  }

  override fun value(int: FunctionId) {
    check(state == 1)
    state++
    out.v32u(int.id)
  }
}
