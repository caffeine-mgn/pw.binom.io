package pw.binom.wasm.writers

import pw.binom.wasm.FunctionId
import pw.binom.wasm.InMemoryWasmOutput
import pw.binom.wasm.WasmOutput
import pw.binom.wasm.readers.ElementSectionReader
import pw.binom.wasm.visitors.ElementSectionVisitor
import pw.binom.wasm.visitors.ExpressionsVisitor

class ElementSectionWriter(private val out: WasmOutput) : ElementSectionVisitor {

  class Type0Writer(private val out: WasmOutput) : ElementSectionVisitor.Type0Visitor {
    private var funcList = ArrayList<Int>()
    private var state = 0
    override fun func(id: FunctionId) {
      check(state == 2)
      funcList += id.id.toInt()
    }

    override fun exp(): ExpressionsVisitor {
      check(state == 1)
      state++
      return ExpressionsWriter(out)
    }

    override fun start() {
      check(state == 0)
      state++
    }

    override fun end() {
      check(state == 2)
      out.v32u(funcList.size.toUInt())
      funcList.forEach {
        out.v32u(it.toUInt())
      }
      funcList.clear()
      state = 0
    }
  }

  private val stream = InMemoryWasmOutput()
  private var count = 0
  private var state = 0

  override fun start() {
    check(state == 0)
    state++
  }

  override fun type0(): ElementSectionVisitor.Type0Visitor {
    check(state == 1)
    count++
    stream.i8u(ElementSectionReader.TYPE0)
    return Type0Writer(stream)
  }

  override fun end() {
    check(state == 1)
    out.v32u(count.toUInt())
    stream.moveTo(out)
    count = 0
    state = 0
  }
}
