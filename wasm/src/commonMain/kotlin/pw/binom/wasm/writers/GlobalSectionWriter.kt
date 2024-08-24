package pw.binom.wasm.writers

import pw.binom.io.ByteArrayOutput
import pw.binom.wasm.StreamWriter
import pw.binom.wasm.visitors.ExpressionsVisitor
import pw.binom.wasm.visitors.GlobalSectionVisitor
import pw.binom.wasm.visitors.ValueVisitor

class GlobalSectionWriter(private val out: StreamWriter) : GlobalSectionVisitor {
  companion object {
    const val NONE = 0
    const val STARTED = 1
    const val TYPE_STARTED = 2
  }

  private val data = ByteArrayOutput()
  private val stream = StreamWriter(data)
  private var count = 0
  private var state = 0

  override fun start() {
    check(state == NONE)
    state = STARTED
  }

  override fun end() {
    check(state == STARTED)
    out.v32u(count.toUInt())
    data.locked {
      out.write(it)
    }
    count = 0
    data.clear()
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
