package pw.binom.wasm.dom.inst

import pw.binom.wasm.Opcodes
import pw.binom.wasm.TypeId
import pw.binom.wasm.visitors.ExpressionsVisitor


sealed interface ArrayOp : Inst {
  val type: TypeId

  class Get(override val type: TypeId) : ArrayOp {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.arrayOp(gcOpcode = Opcodes.GC_ARRAY_GET, type = type)
    }
  }

  class Set(override val type: TypeId) : ArrayOp {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.arrayOp(gcOpcode = Opcodes.GC_ARRAY_SET, type = type)
    }
  }

  class GetS(override val type: TypeId) : ArrayOp {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.arrayOp(gcOpcode = Opcodes.GC_ARRAY_GET_S, type = type)
    }
  }

  class GetU(override val type: TypeId) : ArrayOp {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.arrayOp(gcOpcode = Opcodes.GC_ARRAY_GET_U, type = type)
    }
  }
}
