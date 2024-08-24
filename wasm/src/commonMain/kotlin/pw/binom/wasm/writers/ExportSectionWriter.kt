package pw.binom.wasm.writers

import pw.binom.io.ByteArrayOutput
import pw.binom.wasm.StreamWriter
import pw.binom.wasm.readers.ExportSectionReader
import pw.binom.wasm.visitors.ExportSectionVisitor

class ExportSectionWriter(private val out: StreamWriter) : ExportSectionVisitor {
  private var state = 0
  private val data = ByteArrayOutput()
  private val stream = StreamWriter(data)

  private var count = 0
  override fun start() {
    check(state == 0)
    state++
  }

  override fun end() {
    check(state == 1)
    out.v32u(count.toUInt())
    data.locked {
      out.write(it)
    }
    data.clear()
    state = 0
  }

  override fun func(name: String, value: UInt) {
    check(state == 1)
    count++
    stream.string(name)
    stream.write(ExportSectionReader.FUNC)
    stream.v32u(value)
  }

  override fun table(name: String, value: UInt) {
    check(state == 1)
    count++
    stream.string(name)
    stream.write(ExportSectionReader.TABLE)
    stream.v32u(value)
  }

  override fun memory(name: String, value: UInt) {
    check(state == 1)
    count++
    stream.string(name)
    stream.write(ExportSectionReader.MEM)
    stream.v32u(value)
  }

  override fun global(name: String, value: UInt) {
    check(state == 1)
    count++
    stream.string(name)
    stream.write(ExportSectionReader.GLOBAL)
    stream.v32u(value)
  }
}
