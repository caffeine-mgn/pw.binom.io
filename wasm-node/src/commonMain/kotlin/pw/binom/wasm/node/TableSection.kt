package pw.binom.wasm.node

import pw.binom.wasm.visitors.TableSectionVisitor
import pw.binom.wasm.visitors.ValueVisitor

class TableSection : TableSectionVisitor, MutableList<Table> by ArrayList() {
  private var currentTable: Table? = null

  override fun start() {
    clear()
  }

  override fun end() {
    currentTable = null
  }

  override fun type(): ValueVisitor.RefVisitor {
    val e = Table(type = RefType(), min = 0u, max = null)
    currentTable = e
    this += e
    return e.type()
  }

  override fun limit(inital: UInt) {
    currentTable!!.range(inital)
    super.limit(inital)
  }

  override fun limit(inital: UInt, max: UInt) {
    currentTable!!.range(min = inital, max = max)
  }

  fun accept(visitor: TableSectionVisitor) {
    visitor.start()
    forEach {
      it.type.accept(visitor.type())
      if (it.max == null) {
        visitor.limit(it.min)
      } else {
        visitor.limit(inital = it.min, max = it.max!!)
      }
    }
    visitor.end()
  }
}
