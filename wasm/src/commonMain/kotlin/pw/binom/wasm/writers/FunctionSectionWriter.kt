package pw.binom.wasm.writers

import pw.binom.wasm.InMemoryWasmOutput
import pw.binom.wasm.TypeId
import pw.binom.wasm.WasmOutput
import pw.binom.wasm.visitors.FunctionSectionVisitor

class FunctionSectionWriter(private val out: WasmOutput) : FunctionSectionVisitor {
  private var state = 0
  private var count = 0
  private val stream = InMemoryWasmOutput()

  override fun start() {
    check(state == 0)
    state++
    super.start()
  }

  override fun end() {
    check(state == 1)
    state = 0
    out.v32u(count.toUInt())
    stream.moveTo(out)
    count = 0
  }

  override fun value(int: TypeId) {
    check(state == 1)
    count++
    stream.v32u(int.value)
  }
}
