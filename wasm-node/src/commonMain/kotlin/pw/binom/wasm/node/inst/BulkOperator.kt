package pw.binom.wasm.node.inst

import pw.binom.wasm.Opcodes
import pw.binom.wasm.visitors.ExpressionsVisitor

sealed interface BulkOperator : Inst {
  data object MemoryInit : BulkOperator{
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.bulkOperator(Opcodes.NUMERIC_MEMORY_INIT)
    }
  }
  data object DataDrop : BulkOperator{
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.bulkOperator(Opcodes.NUMERIC_DATA_DROP)
    }
  }
  data object MemoryCopy : BulkOperator{
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.bulkOperator(Opcodes.NUMERIC_MEMORY_COPY)
    }
  }
  data object MemoryFill : BulkOperator{
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.bulkOperator(Opcodes.NUMERIC_MEMORY_FILL)
    }
  }
  data object TableInit : BulkOperator{
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.bulkOperator(Opcodes.NUMERIC_TABLE_INIT)
    }
  }
  data object ElemDrop : BulkOperator{
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.bulkOperator(Opcodes.NUMERIC_ELEM_DROP)
    }
  }
  data object TableCopy : BulkOperator{
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.bulkOperator(Opcodes.NUMERIC_TABLE_COPY)
    }
  }
  data object TableSize : BulkOperator {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.bulkOperator(Opcodes.NUMERIC_TABLE_SIZE)
    }
  }
}
