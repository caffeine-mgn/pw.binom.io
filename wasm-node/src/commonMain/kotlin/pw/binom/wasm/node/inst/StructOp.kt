package pw.binom.wasm.node.inst

import pw.binom.wasm.FieldId
import pw.binom.wasm.Opcodes
import pw.binom.wasm.TypeId
import pw.binom.wasm.visitors.ExpressionsVisitor

sealed interface StructOp : Inst {
  val type: TypeId
  val field: FieldId


  data class GC_STRUCT_SET(override val type: TypeId, override val field: FieldId) : StructOp {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.structOp(
        gcOpcode = Opcodes.GC_STRUCT_SET,
        type = type,
        field = field,
      )
    }
  }

  data class GC_STRUCT_GET(override val type: TypeId, override val field: FieldId) : StructOp {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.structOp(
        gcOpcode = Opcodes.GC_STRUCT_GET,
        type = type,
        field = field,
      )
    }
  }

  data class GC_STRUCT_GET_S(override val type: TypeId, override val field: FieldId) : StructOp {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.structOp(
        gcOpcode = Opcodes.GC_STRUCT_GET_S,
        type = type,
        field = field,
      )
    }
  }

  data class GC_STRUCT_GET_U(override val type: TypeId, override val field: FieldId) : StructOp {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.structOp(
        gcOpcode = Opcodes.GC_STRUCT_GET_U,
        type = type,
        field = field,
      )
    }
  }
}
