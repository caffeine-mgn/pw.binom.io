package pw.binom.wasm.dom

import pw.binom.copyTo
import pw.binom.io.ByteArrayOutput
import pw.binom.io.Input
import pw.binom.io.use
import pw.binom.wasm.visitors.CustomSectionVisitor

class CustomSection : CustomSectionVisitor {

  val elements = ArrayList<CustomBlock>()
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
    elements += CustomBlock(name = name, data = data)
  }

  override fun end() {
    name = ""
    super.end()
  }
}
