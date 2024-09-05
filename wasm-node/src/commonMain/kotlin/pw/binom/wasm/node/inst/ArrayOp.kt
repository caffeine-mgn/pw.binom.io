package pw.binom.wasm.node.inst

import pw.binom.wasm.Opcodes
import pw.binom.wasm.TypeId
import pw.binom.wasm.visitors.ExpressionsVisitor


sealed interface ArrayOp : Inst {
  val type: TypeId

  data class Get(override val type: TypeId) : ArrayOp {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.arrayOp(gcOpcode = Opcodes.GC_ARRAY_GET, type = type)
    }
  }

  data class Set(override val type: TypeId) : ArrayOp {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.arrayOp(gcOpcode = Opcodes.GC_ARRAY_SET, type = type)
    }
  }

  data class GetS(override val type: TypeId) : ArrayOp {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.arrayOp(gcOpcode = Opcodes.GC_ARRAY_GET_S, type = type)
    }
  }

  data class GetU(override val type: TypeId) : ArrayOp {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.arrayOp(gcOpcode = Opcodes.GC_ARRAY_GET_U, type = type)
    }
  }
}
