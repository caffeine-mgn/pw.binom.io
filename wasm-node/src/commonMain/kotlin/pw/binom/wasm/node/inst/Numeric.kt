package pw.binom.wasm.node.inst

import pw.binom.wasm.Opcodes
import pw.binom.wasm.visitors.ExpressionsVisitor

sealed class Numeric : Inst() {
  class I32_CLZ : Numeric() {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.I32_CLZ)
    }
  }
  class I32_CTZ : Numeric() {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.I32_CTZ)
    }
  }
  class I32_POPCNT : Numeric() {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.I32_POPCNT)
    }
  }
  class I32_ADD : Numeric() {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.I32_ADD)
    }
  }
  class I32_SUB : Numeric() {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.I32_SUB)
    }
  }
  class I32_MUL : Numeric() {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.I32_MUL)
    }
  }
  class I32_DIV_S : Numeric() {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.I32_DIV_S)
    }
  }
  class I32_DIV_U : Numeric() {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.I32_DIV_U)
    }
  }
  class I32_REM_S : Numeric() {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.I32_REM_S)
    }
  }
  class I32_REM_U : Numeric() {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.I32_REM_U)
    }
  }
  class I32_AND : Numeric() {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.I32_AND)
    }
  }
  class I32_OR : Numeric() {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.I32_OR)
    }
  }
  class I32_XOR : Numeric() {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.I32_XOR)
    }
  }
  class I32_SHL : Numeric() {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.I32_SHL)
    }
  }
  class I32_SHR_S : Numeric() {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.I32_SHR_S)
    }
  }
  class I32_SHR_U : Numeric() {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.I32_SHR_U)
    }
  }
  class I32_ROTL : Numeric() {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.I32_ROTL)
    }
  }
  class I32_ROTR : Numeric() {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.I32_ROTR)
    }
  }
  class I64_CLZ : Numeric() {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.I64_CLZ)
    }
  }
  class I64_CTZ : Numeric() {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.I64_CTZ)
    }
  }
  class I64_POPCNT : Numeric() {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.I64_POPCNT)
    }
  }
  class I64_ADD : Numeric() {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.I64_ADD)
    }
  }
  class I64_SUB : Numeric() {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.I64_SUB)
    }
  }
  class I64_MUL : Numeric() {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.I64_MUL)
    }
  }
  class I64_DIV_S : Numeric() {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.I64_DIV_S)
    }
  }
  class I64_DIV_U : Numeric() {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.I64_DIV_U)
    }
  }
  class I64_REM_S : Numeric() {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.I64_REM_S)
    }
  }
  class I64_REM_U : Numeric() {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.I64_REM_U)
    }
  }
  class I64_AND : Numeric() {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.I64_AND)
    }
  }
  class I64_OR : Numeric() {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.I64_OR)
    }
  }
  class I64_XOR : Numeric() {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.I64_XOR)
    }
  }
  class I64_SHL : Numeric() {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.I64_SHL)
    }
  }
  class I64_SHR_S : Numeric() {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.I64_SHR_S)
    }
  }
  class I64_SHR_U : Numeric() {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.I64_SHR_U)
    }
  }
  class I64_ROTL : Numeric() {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.I64_ROTL)
    }
  }
  class I64_ROTR : Numeric() {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.I64_ROTR)
    }
  }
  class F32_ABS : Numeric() {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.F32_ABS)
    }
  }
  class F32_NEG : Numeric() {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.F32_NEG)
    }
  }
  class F32_CEIL : Numeric() {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.F32_CEIL)
    }
  }
  class F32_FLOOR : Numeric() {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.F32_FLOOR)
    }
  }
  class F32_TRUNC : Numeric() {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.F32_TRUNC)
    }
  }
  class F32_NEAREST : Numeric() {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.F32_NEAREST)
    }
  }
  class F32_SQRT : Numeric() {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.F32_SQRT)
    }
  }
  class F32_ADD : Numeric() {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.F32_ADD)
    }
  }
  class F32_SUB : Numeric() {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.F32_SUB)
    }
  }
  class F32_MUL : Numeric() {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.F32_MUL)
    }
  }
  class F32_DIV : Numeric() {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.F32_DIV)
    }
  }
  class F32_MIN : Numeric() {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.F32_MIN)
    }
  }
  class F32_MAX : Numeric() {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.F32_MAX)
    }
  }
  class F32_COPYSIGN : Numeric() {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.F32_COPYSIGN)
    }
  }
  class F64_ABS : Numeric() {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.F64_ABS)
    }
  }
  class F64_NEG : Numeric() {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.F64_NEG)
    }
  }
  class F64_CEIL : Numeric() {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.F64_CEIL)
    }
  }
  class F64_FLOOR : Numeric() {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.F64_FLOOR)
    }
  }
  class F64_TRUNC : Numeric() {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.F64_TRUNC)
    }
  }
  class F64_NEAREST : Numeric() {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.F64_NEAREST)
    }
  }
  class F64_SQRT : Numeric() {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.I32_CLZ)
    }
  }
  class F64_ADD : Numeric() {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.I32_CLZ)
    }
  }
  class F64_SUB : Numeric() {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.I32_CLZ)
    }
  }
  class F64_MUL : Numeric() {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.I32_CLZ)
    }
  }
  class F64_DIV : Numeric() {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.I32_CLZ)
    }
  }
  class F64_MIN : Numeric() {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.I32_CLZ)
    }
  }
  class F64_MAX : Numeric() {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.I32_CLZ)
    }
  }
  class F64_COPYSIGN : Numeric() {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.I32_CLZ)
    }
  }
}
