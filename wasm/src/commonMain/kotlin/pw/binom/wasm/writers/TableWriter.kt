package pw.binom.wasm.writers

import pw.binom.wasm.StreamWriter
import pw.binom.wasm.visitors.TableVisitor
import pw.binom.wasm.visitors.ValueVisitor

class TableWriter(private val out: StreamWriter) : TableVisitor {
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
    out.v1u(false)
    out.v32u(min)
  }

  override fun range(min: UInt, max: UInt) {
    check(state == 2)
    state++
    out.v1u(true)
    out.v32u(min)
    out.v32u(max)
  }

  override fun end() {
    check(state == 3)
    state = 0
  }
}
