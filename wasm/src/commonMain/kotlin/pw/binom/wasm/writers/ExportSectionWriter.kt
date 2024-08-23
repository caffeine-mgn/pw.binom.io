package pw.binom.wasm.writers

import pw.binom.wasm.StreamWriter
import pw.binom.wasm.readers.ExportSectionReader
import pw.binom.wasm.visitors.ExportSectionVisitor

class ExportSectionWriter(private val out: StreamWriter) : ExportSectionVisitor {
  private var state = 0
  override fun start(name: String) {
    check(state == 0)
    state++
    out.string(name)
  }

  override fun end() {
    check(state == 2)
    state = 0
  }

  override fun func(value: UInt) {
    check(state == 1)
    state++
    out.write(ExportSectionReader.FUNC)
    out.v32u(value)
  }

  override fun table(value: UInt) {
    check(state == 1)
    state++
    out.write(ExportSectionReader.TABLE)
    out.v32u(value)
  }

  override fun memory(value: UInt) {
    check(state == 1)
    state++
    out.write(ExportSectionReader.MEM)
    out.v32u(value)
  }

  override fun global(value: UInt) {
    check(state == 1)
    state++
    out.write(ExportSectionReader.GLOBAL)
    out.v32u(value)
  }
}
