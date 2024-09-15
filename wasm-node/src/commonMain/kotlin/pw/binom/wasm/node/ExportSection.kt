package pw.binom.wasm.node

import pw.binom.wasm.FunctionId
import pw.binom.wasm.GlobalId
import pw.binom.wasm.MemoryId
import pw.binom.wasm.TableId
import pw.binom.wasm.visitors.ExportSectionVisitor

class ExportSection : ExportSectionVisitor, MutableList<Export> by ArrayList() {
  override fun start() {
    clear()
  }

  override fun end() {
    super.end()
  }

  override fun func(name: String, value: FunctionId) {
    this += Export.Function(name = name, id = value)
  }

  override fun table(name: String, value: TableId) {
    this += Export.Table(name = name, id = value)
  }

  override fun memory(name: String, value: MemoryId) {
    this += Export.Memory(name = name, id = value)
  }

  override fun global(name: String, value: GlobalId) {
    this += Export.Global(name = name, id = value)
  }

  fun accept(visitor: ExportSectionVisitor) {
    visitor.start()
    forEach {
      it.accept(visitor)
    }
    visitor.end()
  }
}
