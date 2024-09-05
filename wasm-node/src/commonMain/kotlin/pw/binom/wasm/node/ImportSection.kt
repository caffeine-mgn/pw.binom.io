package pw.binom.wasm.node

import pw.binom.wasm.FunctionId
import pw.binom.wasm.visitors.ImportSectionVisitor
import pw.binom.wasm.visitors.TableVisitor

class ImportSection : ImportSectionVisitor {
  val elements = ArrayList<Import>()

  override fun start() {
    super.start()
    elements.clear()
  }

  override fun end() {
    super.end()
  }

  override fun function(module: String, field: String, index: FunctionId) {
    elements += Import.Function(
        module = module,
        field = field,
        index = index,
    )
  }

  override fun memory(module: String, field: String, initial: UInt, maximum: UInt) {
    elements += Import.Memory2(
        module = module,
        field = field,
        initial = initial,
        maximum = maximum,
    )
  }

  override fun memory(module: String, field: String, initial: UInt) {
    elements += Import.Memory1(
        module = module,
        field = field,
        initial = initial,
    )
  }

  override fun table(module: String, field: String): TableVisitor {
    val e = Table(type = RefType(), min = 0u, max = null)
    elements + e
    return e
  }

  fun accept(visitor: ImportSectionVisitor) {
    visitor.start()
    elements.forEach {
      it.accept(visitor)
    }
    visitor.end()
  }

  override fun global(module: String, field: String): ImportSectionVisitor.GlobalVisitor {
    val e = ImportGlobal(module = module, field = field)
    elements += e
    return e
  }
}
