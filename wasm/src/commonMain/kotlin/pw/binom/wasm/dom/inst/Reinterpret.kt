package pw.binom.wasm.dom.inst

import pw.binom.wasm.Opcodes
import pw.binom.wasm.visitors.ExpressionsVisitor

sealed interface Reinterpret : Inst {
  object I32ToF32 : Reinterpret {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.reinterpret(Opcodes.I32_REINTERPRET_F32)
    }
  }

  object I64ToF64 : Reinterpret {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.reinterpret(Opcodes.I64_REINTERPRET_F64)
    }
  }

  object F32ToI32 : Reinterpret {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.reinterpret(Opcodes.F32_REINTERPRET_I32)
    }
  }

  object F64ToI64 : Reinterpret {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.reinterpret(Opcodes.F64_REINTERPRET_I64)
    }
  }
}
