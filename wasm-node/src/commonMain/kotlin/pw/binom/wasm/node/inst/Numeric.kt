package pw.binom.wasm.node.inst

import pw.binom.wasm.Opcodes
import pw.binom.wasm.visitors.ExpressionsVisitor

sealed interface Numeric : Inst {
  data object I32_CLZ : Numeric {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.I32_CLZ)
    }
  }
  data object I32_CTZ : Numeric {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.I32_CTZ)
    }
  }
  data object I32_POPCNT : Numeric {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.I32_POPCNT)
    }
  }
  data object I32_ADD : Numeric {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.I32_ADD)
    }
  }
  data object I32_SUB : Numeric {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.I32_SUB)
    }
  }
  data object I32_MUL : Numeric {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.I32_MUL)
    }
  }
  data object I32_DIV_S : Numeric {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.I32_DIV_S)
    }
  }
  data object I32_DIV_U : Numeric {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.I32_DIV_U)
    }
  }
  data object I32_REM_S : Numeric {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.I32_REM_S)
    }
  }
  data object I32_REM_U : Numeric {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.I32_REM_U)
    }
  }
  data object I32_AND : Numeric {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.I32_AND)
    }
  }
  data object I32_OR : Numeric {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.I32_OR)
    }
  }
  data object I32_XOR : Numeric {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.I32_XOR)
    }
  }
  data object I32_SHL : Numeric {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.I32_SHL)
    }
  }
  data object I32_SHR_S : Numeric {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.I32_SHR_S)
    }
  }
  data object I32_SHR_U : Numeric {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.I32_SHR_U)
    }
  }
  data object I32_ROTL : Numeric {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.I32_ROTL)
    }
  }
  data object I32_ROTR : Numeric {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.I32_ROTR)
    }
  }
  data object I64_CLZ : Numeric {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.I64_CLZ)
    }
  }
  data object I64_CTZ : Numeric {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.I64_CTZ)
    }
  }
  data object I64_POPCNT : Numeric {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.I64_POPCNT)
    }
  }
  data object I64_ADD : Numeric {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.I64_ADD)
    }
  }
  data object I64_SUB : Numeric {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.I64_SUB)
    }
  }
  data object I64_MUL : Numeric {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.I64_MUL)
    }
  }
  data object I64_DIV_S : Numeric {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.I64_DIV_S)
    }
  }
  data object I64_DIV_U : Numeric {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.I64_DIV_U)
    }
  }
  data object I64_REM_S : Numeric {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.I64_REM_S)
    }
  }
  data object I64_REM_U : Numeric {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.I64_REM_U)
    }
  }
  data object I64_AND : Numeric {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.I64_AND)
    }
  }
  data object I64_OR : Numeric {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.I64_OR)
    }
  }
  data object I64_XOR : Numeric {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.I64_XOR)
    }
  }
  data object I64_SHL : Numeric {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.I64_SHL)
    }
  }
  data object I64_SHR_S : Numeric {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.I64_SHR_S)
    }
  }
  data object I64_SHR_U : Numeric {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.I64_SHR_U)
    }
  }
  data object I64_ROTL : Numeric {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.I64_ROTL)
    }
  }
  data object I64_ROTR : Numeric {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.I64_ROTR)
    }
  }
  data object F32_ABS : Numeric {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.F32_ABS)
    }
  }
  data object F32_NEG : Numeric {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.F32_NEG)
    }
  }
  data object F32_CEIL : Numeric {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.F32_CEIL)
    }
  }
  data object F32_FLOOR : Numeric {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.F32_FLOOR)
    }
  }
  data object F32_TRUNC : Numeric {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.F32_TRUNC)
    }
  }
  data object F32_NEAREST : Numeric {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.F32_NEAREST)
    }
  }
  data object F32_SQRT : Numeric {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.F32_SQRT)
    }
  }
  data object F32_ADD : Numeric {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.F32_ADD)
    }
  }
  data object F32_SUB : Numeric {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.F32_SUB)
    }
  }
  data object F32_MUL : Numeric {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.F32_MUL)
    }
  }
  data object F32_DIV : Numeric {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.F32_DIV)
    }
  }
  data object F32_MIN : Numeric {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.F32_MIN)
    }
  }
  data object F32_MAX : Numeric {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.F32_MAX)
    }
  }
  data object F32_COPYSIGN : Numeric {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.F32_COPYSIGN)
    }
  }
  data object F64_ABS : Numeric {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.F64_ABS)
    }
  }
  data object F64_NEG : Numeric {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.F64_NEG)
    }
  }
  data object F64_CEIL : Numeric {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.F64_CEIL)
    }
  }
  data object F64_FLOOR : Numeric {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.F64_FLOOR)
    }
  }
  data object F64_TRUNC : Numeric {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.F64_TRUNC)
    }
  }
  data object F64_NEAREST : Numeric {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.F64_NEAREST)
    }
  }
  data object F64_SQRT : Numeric {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.I32_CLZ)
    }
  }
  data object F64_ADD : Numeric {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.I32_CLZ)
    }
  }
  data object F64_SUB : Numeric {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.I32_CLZ)
    }
  }
  data object F64_MUL : Numeric {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.I32_CLZ)
    }
  }
  data object F64_DIV : Numeric {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.I32_CLZ)
    }
  }
  data object F64_MIN : Numeric {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.I32_CLZ)
    }
  }
  data object F64_MAX : Numeric {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.I32_CLZ)
    }
  }
  data object F64_COPYSIGN : Numeric {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.numeric(Opcodes.I32_CLZ)
    }
  }
}
