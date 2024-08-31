package pw.binom.wasm.nodes

import pw.binom.wasm.FunctionId
import pw.binom.wasm.GlobalId
import pw.binom.wasm.MemoryId
import pw.binom.wasm.TableId
import pw.binom.wasm.visitors.ExportSectionVisitor

class ExportSection : ExportSectionVisitor {
  val elements = ArrayList<Export>()

  val isEmpty
    get() = elements.isEmpty()

  val isNotEmpty
    get() = elements.isNotEmpty()

  override fun start() {
    elements.clear()
  }

  override fun end() {
    super.end()
  }

  override fun func(name: String, value: FunctionId) {
    elements += Export.Function(name = name, id = value)
  }

  override fun table(name: String, value: TableId) {
    elements += Export.Table(name = name, id = value)
  }

  override fun memory(name: String, value: MemoryId) {
    elements += Export.Memory(name = name, id = value)
  }

  override fun global(name: String, value: GlobalId) {
    elements += Export.Global(name = name, id = value)
  }

  fun accept(visitor: ExportSectionVisitor) {
    visitor.start()
    elements.forEach {
      it.accept(visitor)
    }
    visitor.end()
  }
}
