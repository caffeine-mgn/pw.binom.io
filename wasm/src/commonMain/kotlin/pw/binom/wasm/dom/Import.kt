package pw.binom.wasm.dom

import pw.binom.wasm.FunctionId
import pw.binom.wasm.visitors.ImportSectionVisitor
import pw.binom.wasm.visitors.TableVisitor
import pw.binom.wasm.visitors.ValueVisitor

sealed interface Import {
  var module: String
  var field: String
  fun accept(visitor: ImportSectionVisitor)

  class Function(
    override var module: String,
    override var field: String,
    var index: FunctionId,
  ) : Import {
    override fun accept(visitor: ImportSectionVisitor) {
      visitor.function(module = module, field = field, index = index)
    }
  }

  class Memory1(
    override var module: String,
    override var field: String,
    var initial: UInt,
  ) : Import {
    override fun accept(visitor: ImportSectionVisitor) {
      visitor.memory(module = module, field = field, initial = initial)
    }
  }

  class Memory2(
    override var module: String,
    override var field: String,
    var initial: UInt,
    var maximum: UInt,
  ) : Import {
    override fun accept(visitor: ImportSectionVisitor) {
      visitor.memory(module = module, field = field, initial = initial, maximum = maximum)
    }
  }

  class Table(
    override var module: String,
    override var field: String,
    val table: pw.binom.wasm.dom.Table,
  ) : Import, TableVisitor {

    override fun range(min: UInt, max: UInt) {
      table.min = min
      table.max = max
    }

    override fun range(min: UInt) {
      table.min = min
      table.max = null
    }

    override fun type(): ValueVisitor.RefVisitor {
      return table.type
    }

    override fun accept(visitor: ImportSectionVisitor) {
      val v = visitor.table(
        module = module,
        field = field,
      )
      v.start()
      table.type.accept(v.type())
      if (table.max == null) {
        v.range(min = table.min)
      } else {
        v.range(min = table.min, max = table.max!!)
      }
      v.end()
    }

  }
}
