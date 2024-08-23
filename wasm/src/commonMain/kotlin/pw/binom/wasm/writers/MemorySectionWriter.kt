package pw.binom.wasm.writers

import pw.binom.io.ByteArrayOutput
import pw.binom.wasm.StreamWriter
import pw.binom.wasm.visitors.MemorySectionVisitor

class MemorySectionWriter(private val out: StreamWriter) : MemorySectionVisitor {

  private val data = ByteArrayOutput()
  private val stream = StreamWriter(data)
  private var count = 0
  private var state = 0

  override fun start() {
    check(state == 0)
    state++
  }

  override fun memory(inital: UInt) {
    check(state == 1)
    count++
    stream.limit(inital)
  }

  override fun memory(inital: UInt, max: UInt) {
    check(state == 1)
    count++
    stream.limit(inital = inital, max = max)
  }

  override fun end() {
    check(state == 1)
    state = 0
    out.v32u(count.toUInt())
    data.locked {
      out.writeFully(it)
    }
    data.clear()
  }
}
