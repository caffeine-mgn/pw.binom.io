package pw.binom.wasm.writers

import pw.binom.io.ByteArrayOutput
import pw.binom.wasm.InMemoryWasmOutput
import pw.binom.wasm.StreamWriter
import pw.binom.wasm.WasmOutput
import pw.binom.wasm.visitors.ExpressionsVisitor
import pw.binom.wasm.visitors.GlobalSectionVisitor
import pw.binom.wasm.visitors.ValueVisitor
import pw.binom.wasm.write

class GlobalSectionWriter(private val out: WasmOutput) : GlobalSectionVisitor {
  companion object {
    const val NONE = 0
    const val STARTED = 1
    const val TYPE_STARTED = 2
  }

  private val stream = InMemoryWasmOutput()
  private var count = 0
  private var state = 0

  override fun start() {
    check(state == NONE)
    state = STARTED
  }

  override fun end() {
    check(state == STARTED)
    out.v32u(count.toUInt())
    out.write(stream)
    stream.clear()
    count = 0
    state = NONE
  }

  override fun type(): ValueVisitor {
    check(state == STARTED)
    count++
    state = TYPE_STARTED
    return ValueWriter(stream)
  }

  override fun code(mutable: Boolean): ExpressionsVisitor {
    check(state == TYPE_STARTED)
    state = STARTED
    return ExpressionsWriter(stream)
  }
}
