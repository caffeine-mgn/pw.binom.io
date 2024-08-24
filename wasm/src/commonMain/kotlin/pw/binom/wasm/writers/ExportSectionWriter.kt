package pw.binom.wasm.writers

import pw.binom.io.ByteArrayOutput
import pw.binom.wasm.InMemoryWasmOutput
import pw.binom.wasm.StreamWriter
import pw.binom.wasm.WasmOutput
import pw.binom.wasm.readers.ExportSectionReader
import pw.binom.wasm.visitors.ExportSectionVisitor
import pw.binom.wasm.write

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
    out.write(stream)
    stream.clear()
    state = 0
  }

  override fun func(name: String, value: UInt) {
    check(state == 1)
    count++
    stream.string(name)
    stream.i8u(ExportSectionReader.FUNC)
    stream.v32u(value)
  }

  override fun table(name: String, value: UInt) {
    check(state == 1)
    count++
    stream.string(name)
    stream.i8u(ExportSectionReader.TABLE)
    stream.v32u(value)
  }

  override fun memory(name: String, value: UInt) {
    check(state == 1)
    count++
    stream.string(name)
    stream.i8u(ExportSectionReader.MEM)
    stream.v32u(value)
  }

  override fun global(name: String, value: UInt) {
    check(state == 1)
    count++
    stream.string(name)
    stream.i8u(ExportSectionReader.GLOBAL)
    stream.v32u(value)
  }
}
