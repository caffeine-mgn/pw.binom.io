package pw.binom.wasm.writers

import pw.binom.io.ByteArrayOutput
import pw.binom.wasm.*
import pw.binom.wasm.visitors.TableSectionVisitor
import pw.binom.wasm.visitors.ValueVisitor

class TableSectionWriter(private val out: WasmOutput) : TableSectionVisitor {
  private val stream = InMemoryWasmOutput()
  private var count = 0
  private var state = 0
  override fun start() {
    check(state == 0)
    state++
  }

  override fun end() {
    check(state == 1)
    state = 0
    out.v32u(count.toUInt())
    out.write(stream)
    stream.clear()
  }

  override fun type(): ValueVisitor.RefVisitor {
    check(state == 1)
    state++
    count++
    return ValueWriter(stream)
  }

  override fun limit(inital: UInt) {
    check(state == 2)
    state--
    out.limit(inital = inital)
  }

  override fun limit(inital: UInt, max: UInt) {
    check(state == 2)
    state--
    out.limit(inital = inital, max = max)
  }
}
