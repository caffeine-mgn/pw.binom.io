package pw.binom.wasm.node.inst

import pw.binom.wasm.Opcodes
import pw.binom.wasm.visitors.ExpressionsVisitor

sealed class Reinterpret : Inst() {
  class I32ToF32 : Reinterpret() {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.reinterpret(Opcodes.I32_REINTERPRET_F32)
    }
  }

  class I64ToF64 : Reinterpret() {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.reinterpret(Opcodes.I64_REINTERPRET_F64)
    }
  }

  class F32ToI32 : Reinterpret() {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.reinterpret(Opcodes.F32_REINTERPRET_I32)
    }
  }

  class F64ToI64 : Reinterpret() {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.reinterpret(Opcodes.F64_REINTERPRET_I64)
    }
  }
}
