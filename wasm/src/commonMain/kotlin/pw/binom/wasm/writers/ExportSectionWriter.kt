package pw.binom.wasm.writers

import pw.binom.wasm.*
import pw.binom.wasm.readers.ExportSectionReader
import pw.binom.wasm.visitors.ExportSectionVisitor

class ExportSectionWriter(private val out: WasmOutput) : ExportSectionVisitor {
  private var state = 0
  private val stream = InMemoryWasmOutput()

  private var count = 0
  override fun start() {
    check(state == 0)
    state++
  }

  override fun end() {
    check(state == 1)
    out.v32u(count.toUInt())
    stream.moveTo(out)
    state = 0
  }

  override fun func(name: String, value: FunctionId) {
    check(state == 1)
    count++
    stream.string(name)
    stream.i8u(ExportSectionReader.FUNC)
    stream.v32u(value.id)
  }

  override fun table(name: String, value: TableId) {
    check(state == 1)
    count++
    stream.string(name)
    stream.i8u(ExportSectionReader.TABLE)
    stream.v32u(value.id)
  }

  override fun memory(name: String, value: MemoryId) {
    check(state == 1)
    count++
    stream.string(name)
    stream.i8u(ExportSectionReader.MEM)
    stream.v32u(value.raw)
  }

  override fun global(name: String, value: GlobalId) {
    check(state == 1)
    count++
    stream.string(name)
    stream.i8u(ExportSectionReader.GLOBAL)
    stream.v32u(value.id)
  }
}
