package pw.binom.wasm.writers

import pw.binom.wasm.WasmOutput
import pw.binom.wasm.limit
import pw.binom.wasm.visitors.TableVisitor
import pw.binom.wasm.visitors.ValueVisitor

class TableWriter(private val out: WasmOutput) : TableVisitor {
  private var state = 0
  override fun start() {
    check(state == 0)
    state++
  }

  override fun type(): ValueVisitor.RefVisitor {
    check(state == 1)
    state++
    return ValueWriter(out)
  }

  override fun range(min: UInt) {
    check(state == 2)
    state++
    out.limit(inital = min)
  }

  override fun range(min: UInt, max: UInt) {
    check(state == 2)
    state++
    out.limit(inital = min, max = max)
  }

  override fun end() {
    check(state == 3)
    state = 0
  }
}
