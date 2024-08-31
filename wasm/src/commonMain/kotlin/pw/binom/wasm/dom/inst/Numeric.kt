package pw.binom.wasm.dom.inst

import pw.binom.wasm.Opcodes
import pw.binom.wasm.visitors.ExpressionsVisitor

sealed interface Numeric : Inst {
  object I32_CLZ : Numeric {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.I32_CLZ)
    }
  }
  object I32_CTZ : Numeric {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.I32_CTZ)
    }
  }
  object I32_POPCNT : Numeric {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.I32_POPCNT)
    }
  }
  object I32_ADD : Numeric {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.I32_ADD)
    }
  }
  object I32_SUB : Numeric {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.I32_SUB)
    }
  }
  object I32_MUL : Numeric {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.I32_MUL)
    }
  }
  object I32_DIV_S : Numeric {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.I32_DIV_S)
    }
  }
  object I32_DIV_U : Numeric {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.I32_DIV_U)
    }
  }
  object I32_REM_S : Numeric {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.I32_REM_S)
    }
  }
  object I32_REM_U : Numeric {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.I32_REM_U)
    }
  }
  object I32_AND : Numeric {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.I32_AND)
    }
  }
  object I32_OR : Numeric {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.I32_OR)
    }
  }
  object I32_XOR : Numeric {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.I32_XOR)
    }
  }
  object I32_SHL : Numeric {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.I32_SHL)
    }
  }
  object I32_SHR_S : Numeric {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.I32_SHR_S)
    }
  }
  object I32_SHR_U : Numeric {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.I32_SHR_U)
    }
  }
  object I32_ROTL : Numeric {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.I32_ROTL)
    }
  }
  object I32_ROTR : Numeric {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.I32_ROTR)
    }
  }
  object I64_CLZ : Numeric {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.I64_CLZ)
    }
  }
  object I64_CTZ : Numeric {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.I64_CTZ)
    }
  }
  object I64_POPCNT : Numeric {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.I64_POPCNT)
    }
  }
  object I64_ADD : Numeric {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.I64_ADD)
    }
  }
  object I64_SUB : Numeric {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.I64_SUB)
    }
  }
  object I64_MUL : Numeric {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.I64_MUL)
    }
  }
  object I64_DIV_S : Numeric {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.I64_DIV_S)
    }
  }
  object I64_DIV_U : Numeric {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.I64_DIV_U)
    }
  }
  object I64_REM_S : Numeric {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.I64_REM_S)
    }
  }
  object I64_REM_U : Numeric {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.I64_REM_U)
    }
  }
  object I64_AND : Numeric {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.I64_AND)
    }
  }
  object I64_OR : Numeric {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.I64_OR)
    }
  }
  object I64_XOR : Numeric {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.I64_XOR)
    }
  }
  object I64_SHL : Numeric {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.I64_SHL)
    }
  }
  object I64_SHR_S : Numeric {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.I64_SHR_S)
    }
  }
  object I64_SHR_U : Numeric {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.I64_SHR_U)
    }
  }
  object I64_ROTL : Numeric {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.I64_ROTL)
    }
  }
  object I64_ROTR : Numeric {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.I64_ROTR)
    }
  }
  object F32_ABS : Numeric {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.F32_ABS)
    }
  }
  object F32_NEG : Numeric {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.F32_NEG)
    }
  }
  object F32_CEIL : Numeric {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.F32_CEIL)
    }
  }
  object F32_FLOOR : Numeric {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.F32_FLOOR)
    }
  }
  object F32_TRUNC : Numeric {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.F32_TRUNC)
    }
  }
  object F32_NEAREST : Numeric {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.F32_NEAREST)
    }
  }
  object F32_SQRT : Numeric {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.F32_SQRT)
    }
  }
  object F32_ADD : Numeric {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.F32_ADD)
    }
  }
  object F32_SUB : Numeric {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.F32_SUB)
    }
  }
  object F32_MUL : Numeric {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.F32_MUL)
    }
  }
  object F32_DIV : Numeric {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.F32_DIV)
    }
  }
  object F32_MIN : Numeric {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.F32_MIN)
    }
  }
  object F32_MAX : Numeric {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.F32_MAX)
    }
  }
  object F32_COPYSIGN : Numeric {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.F32_COPYSIGN)
    }
  }
  object F64_ABS : Numeric {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.F64_ABS)
    }
  }
  object F64_NEG : Numeric {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.F64_NEG)
    }
  }
  object F64_CEIL : Numeric {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.F64_CEIL)
    }
  }
  object F64_FLOOR : Numeric {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.F64_FLOOR)
    }
  }
  object F64_TRUNC : Numeric {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.F64_TRUNC)
    }
  }
  object F64_NEAREST : Numeric {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.F64_NEAREST)
    }
  }
  object F64_SQRT : Numeric {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.I32_CLZ)
    }
  }
  object F64_ADD : Numeric {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.I32_CLZ)
    }
  }
  object F64_SUB : Numeric {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.I32_CLZ)
    }
  }
  object F64_MUL : Numeric {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.I32_CLZ)
    }
  }
  object F64_DIV : Numeric {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.I32_CLZ)
    }
  }
  object F64_MIN : Numeric {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.I32_CLZ)
    }
  }
  object F64_MAX : Numeric {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.I32_CLZ)
    }
  }
  object F64_COPYSIGN : Numeric {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.I32_CLZ)
    }
  }
}
