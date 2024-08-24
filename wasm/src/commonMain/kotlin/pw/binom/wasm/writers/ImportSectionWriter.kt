package pw.binom.wasm.writers

import pw.binom.wasm.*
import pw.binom.wasm.visitors.ImportSectionVisitor
import pw.binom.wasm.visitors.TableVisitor

class ImportSectionWriter(private val out: WasmOutput) : ImportSectionVisitor {
  private var state = 0
  private val stream = InMemoryWasmOutput()
  private var count = 0
  override fun start() {
    check(state == 0)
    state++
  }

  override fun end() {
    check(state == 1)
    state = 0
    out.v32u(count.toUInt())
    stream.moveTo(out)
  }

  override fun function(module: String, field: String, index: FunctionId) {
    check(state == 1)
    count++
    stream.string(module)
    stream.string(field)
    stream.i8s(0)
    stream.v32u(index.id)
  }

  override fun table(module: String, field: String): TableVisitor {
    check(state == 1)
    count++
    stream.string(module)
    stream.string(field)
    stream.i8s(1)
    return TableWriter(stream)
  }

  override fun memory(module: String, field: String, initial: UInt) {
    check(state == 1)
    count++
    stream.string(module)
    stream.string(field)
    stream.i8s(2)
    stream.limit(inital = initial)
  }

  override fun memory(module: String, field: String, initial: UInt, maximum: UInt) {
    check(state == 1)
    count++
    stream.string(module)
    stream.string(field)
    stream.i8s(2)
    stream.limit(inital = initial, max = maximum)
  }
}
