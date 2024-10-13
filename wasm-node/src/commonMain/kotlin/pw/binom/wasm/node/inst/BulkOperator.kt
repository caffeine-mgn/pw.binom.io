package pw.binom.wasm.node.inst

import pw.binom.wasm.Opcodes
import pw.binom.wasm.visitors.ExpressionsVisitor

sealed class BulkOperator : Inst() {
  class MemoryInit : BulkOperator() {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.bulkOperator(Opcodes.NUMERIC_MEMORY_INIT)
    }
  }

  class DataDrop : BulkOperator() {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.bulkOperator(Opcodes.NUMERIC_DATA_DROP)
    }
  }

  class MemoryCopy : BulkOperator() {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.bulkOperator(Opcodes.NUMERIC_MEMORY_COPY)
    }
  }

  class MemoryFill : BulkOperator() {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.bulkOperator(Opcodes.NUMERIC_MEMORY_FILL)
    }
  }

  class TableInit : BulkOperator() {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.bulkOperator(Opcodes.NUMERIC_TABLE_INIT)
    }
  }

  class ElemDrop : BulkOperator() {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.bulkOperator(Opcodes.NUMERIC_ELEM_DROP)
    }
  }

  class TableCopy : BulkOperator() {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.bulkOperator(Opcodes.NUMERIC_TABLE_COPY)
    }
  }

  class TableSize : BulkOperator() {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.bulkOperator(Opcodes.NUMERIC_TABLE_SIZE)
    }
  }
}
