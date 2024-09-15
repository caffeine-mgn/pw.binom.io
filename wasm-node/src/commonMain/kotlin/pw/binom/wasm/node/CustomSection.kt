package pw.binom.wasm.node

import pw.binom.copyTo
import pw.binom.io.ByteArrayOutput
import pw.binom.io.Input
import pw.binom.io.use
import pw.binom.wasm.visitors.CustomSectionVisitor
import pw.binom.wasm.visitors.WasmVisitor

class CustomSection : CustomSectionVisitor, MutableList<CustomBlock> by ArrayList() {

  private var name = ""

  override fun start(name: String) {
    super.start(name)
    this.name = name
  }

  override fun data(input: Input) {
    super.data(input)
    val data = ByteArrayOutput().use {
      input.copyTo(it)
      it.toByteArray()
    }
    this += CustomBlock(name = name, data = data)
  }

  override fun end() {
    name = ""
    super.end()
  }

  fun accept(visitor: WasmVisitor) {
    forEach { block ->
      block.accept(visitor.customSection())
    }
  }
}
