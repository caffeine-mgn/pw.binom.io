package pw.binom.wasm.writers

import pw.binom.collections.LinkedList
import pw.binom.wasm.TypeId
import pw.binom.wasm.WasmOutput
import pw.binom.wasm.visitors.TagSectionVisitor

class TagSectionWriter(private val out: WasmOutput) : TagSectionVisitor {
  private val types = LinkedList<Int>()
  private var state = 0

  override fun start() {
    check(state == 0)
    state++
    super.start()
  }

  override fun end() {
    check(state == 1)
    state = 0
    out.v32u(types.size.toUInt())
    types.forEach {
      out.i8u(0u)
      out.v32u(it.toUInt())
    }
    super.end()
  }

  override fun tag(type: TypeId) {
    check(state == 1)
    types += type.value.toInt()
  }
}
