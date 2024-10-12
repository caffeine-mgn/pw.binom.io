package pw.binom.wasm.node

import pw.binom.wasm.FunctionId
import pw.binom.wasm.TypeId
import pw.binom.wasm.visitors.ImportSectionVisitor
import pw.binom.wasm.visitors.TableVisitor
import pw.binom.wasm.visitors.ValueVisitor
import kotlin.js.JsName

sealed class Import {
  abstract var module: String
  abstract var field: String
  abstract fun accept(visitor: ImportSectionVisitor)

  class Function(
    override var module: String,
    override var field: String,
    var index: TypeId,
  ) : Import() {
    override fun accept(visitor: ImportSectionVisitor) {
      visitor.function(module = module, field = field, index = index)
    }
  }

  sealed class Memory:Import(){
    abstract var initial: UInt
  }

  class Memory1(
    override var module: String,
    override var field: String,
    override var initial: UInt,
  ) : Memory() {
    override fun accept(visitor: ImportSectionVisitor) {
      visitor.memory(module = module, field = field, initial = initial)
    }
  }

  class Memory2(
    override var module: String,
    override var field: String,
    override var initial: UInt,
    var maximum: UInt,
  ) : Memory() {
    override fun accept(visitor: ImportSectionVisitor) {
      visitor.memory(module = module, field = field, initial = initial, maximum = maximum)
    }
  }

  class Table(
      override var module: String,
      override var field: String,
      val table: pw.binom.wasm.node.Table,
  ) : Import(), TableVisitor {

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

  class Global(
    override var module: String,
    override var field: String,
  ) : ImportSectionVisitor.GlobalVisitor, Import() {
    @JsName("typeF")
    var type = ValueType()
    var mutable = false

    override fun type(): ValueVisitor = type

    override fun mutable(value: Boolean) {
      this.mutable = value
    }

    fun accept(visitor: ImportSectionVisitor.GlobalVisitor) {
      visitor.start()
      type.accept(visitor.type())
      visitor.mutable(mutable)
      visitor.end()
    }

    override fun accept(visitor: ImportSectionVisitor) {
      accept(
        visitor.global(
          module = module,
          field = field
        )
      )
    }
  }
}
