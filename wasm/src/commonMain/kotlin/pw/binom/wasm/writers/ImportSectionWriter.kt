package pw.binom.wasm.writers

import pw.binom.wasm.FunctionId
import pw.binom.wasm.StreamWriter
import pw.binom.wasm.WasmOutput
import pw.binom.wasm.limit
import pw.binom.wasm.visitors.ImportSectionVisitor
import pw.binom.wasm.visitors.TableVisitor

class ImportSectionWriter(private val out: WasmOutput) : ImportSectionVisitor {
  private var state = 0
  override fun start() {
    check(state == 0)
    state++
  }

  override fun end() {
    check(state == 2)
    state = 0
  }

  override fun function(module: String, field: String, index: FunctionId) {
    check(state == 1)
    state++
    out.string(module)
    out.string(field)
    out.i8s(0)
    out.v32u(index.id)
  }

  override fun table(module: String, field: String): TableVisitor {
    check(state == 1)
    state++
    out.string(module)
    out.string(field)
    out.i8s(1)
    return TableWriter(out)
  }

  override fun memory(module: String, field: String, initial: UInt) {
    check(state == 1)
    state++
    out.string(module)
    out.string(field)
    out.i8s(2)
    out.limit(inital = initial)
  }

  override fun memory(module: String, field: String, initial: UInt, maximum: UInt) {
    check(state == 1)
    state++
    out.string(module)
    out.string(field)
    out.i8s(2)
    out.limit(inital = initial, max = maximum)
  }
}
