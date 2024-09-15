package pw.binom.wasm.node

import pw.binom.wasm.TypeId
import pw.binom.wasm.visitors.ImportSectionVisitor
import pw.binom.wasm.visitors.TableVisitor

class ImportSection : ImportSectionVisitor,MutableList<Import> by ArrayList() {

  override fun start() {
    super.start()
    clear()
  }

  override fun end() {
    super.end()
  }

  override fun function(module: String, field: String, index: TypeId) {
    this += Import.Function(
      module = module,
      field = field,
      index = index,
    )
  }

  override fun memory(module: String, field: String, initial: UInt, maximum: UInt) {
    this += Import.Memory2(
      module = module,
      field = field,
      initial = initial,
      maximum = maximum,
    )
  }

  override fun memory(module: String, field: String, initial: UInt) {
    this += Import.Memory1(
      module = module,
      field = field,
      initial = initial,
    )
  }

  override fun table(module: String, field: String): TableVisitor {
    val e = Import.Table(module = module, field = field, table = Table(type = RefType(), min = 0u, max = null))
    this += e
    return e
  }

  override fun global(module: String, field: String): ImportSectionVisitor.GlobalVisitor {
    val e = Import.Global(module = module, field = field)
    this += e
    return e
  }

  fun accept(visitor: ImportSectionVisitor) {
    visitor.start()
    forEach {
      it.accept(visitor)
    }
    visitor.end()
  }
}
