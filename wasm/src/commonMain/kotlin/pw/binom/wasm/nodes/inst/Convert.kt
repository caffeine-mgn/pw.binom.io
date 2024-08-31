package pw.binom.wasm.nodes.inst

import pw.binom.wasm.Opcodes
import pw.binom.wasm.visitors.ExpressionsVisitor

sealed interface Convert : Inst {
  object I32_WRAP_I64 : Convert {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.convert(Opcodes.I32_WRAP_I64)
    }
  }

  object I32_TRUNC_S_F32 : Convert {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.convert(Opcodes.I32_TRUNC_S_F32)
    }
  }

  object I32_TRUNC_U_F32 : Convert {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.convert(Opcodes.I32_TRUNC_U_F32)
    }
  }

  object I32_TRUNC_S_F64 : Convert {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.convert(Opcodes.I32_TRUNC_S_F64)
    }
  }

  object I32_TRUNC_U_F64 : Convert {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.convert(Opcodes.I32_TRUNC_U_F64)
    }
  }

  object I64_EXTEND_S_I32 : Convert {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.convert(Opcodes.I64_EXTEND_S_I32)
    }
  }

  object I64_EXTEND_U_I32 : Convert {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.convert(Opcodes.I64_EXTEND_U_I32)
    }
  }

  object I64_TRUNC_S_F32 : Convert {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.convert(Opcodes.I64_TRUNC_S_F32)
    }
  }

  object I64_TRUNC_U_F32 : Convert {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.convert(Opcodes.I64_TRUNC_U_F32)
    }
  }

  object I64_TRUNC_S_F64 : Convert {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.convert(Opcodes.I64_TRUNC_S_F64)
    }
  }

  object I64_TRUNC_U_F64 : Convert {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.convert(Opcodes.I64_TRUNC_U_F64)
    }
  }

  object F32_CONVERT_S_I32 : Convert {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.convert(Opcodes.F32_CONVERT_S_I32)
    }
  }

  object F32_CONVERT_U_I32 : Convert {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.convert(Opcodes.F32_CONVERT_U_I32)
    }
  }

  object F32_CONVERT_S_I64 : Convert {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.convert(Opcodes.F32_CONVERT_S_I64)
    }
  }

  object F32_CONVERT_U_I64 : Convert {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.convert(Opcodes.F32_CONVERT_U_I64)
    }
  }

  object F32_DEMOTE_F64 : Convert {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.convert(Opcodes.F32_DEMOTE_F64)
    }
  }

  object F64_CONVERT_S_I32 : Convert {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.convert(Opcodes.F64_CONVERT_S_I32)
    }
  }

  object F64_CONVERT_U_I32 : Convert {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.convert(Opcodes.F64_CONVERT_U_I32)
    }
  }

  object F64_CONVERT_S_I64 : Convert {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.convert(Opcodes.F64_CONVERT_S_I64)
    }
  }

  object F64_CONVERT_U_I64 : Convert {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.convert(Opcodes.F64_CONVERT_U_I64)
    }
  }

  object F64_PROMOTE_F32 : Convert {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.convert(Opcodes.F64_PROMOTE_F32)
    }
  }

  object GC_ANY_CONVERT_EXTERN : Convert {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.gcConvert(Opcodes.GC_ANY_CONVERT_EXTERN)
    }
  }

  object GC_EXTERN_CONVERT_ANY : Convert {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.gcConvert(Opcodes.GC_EXTERN_CONVERT_ANY)
    }
  }

  object NUMERIC_I32S_CONVERT_SAT_F32: Convert {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.convertNumeric(Opcodes.NUMERIC_I32S_CONVERT_SAT_F32)
    }
  }
  object NUMERIC_I32U_CONVERT_SAT_F32: Convert {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.convertNumeric(Opcodes.NUMERIC_I32U_CONVERT_SAT_F32)
    }
  }
  object NUMERIC_I32S_CONVERT_SAT_F64: Convert {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.convertNumeric(Opcodes.NUMERIC_I32S_CONVERT_SAT_F64)
    }
  }
  object NUMERIC_I32U_CONVERT_SAT_F64: Convert {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.convertNumeric(Opcodes.NUMERIC_I32U_CONVERT_SAT_F64)
    }
  }
  object NUMERIC_I64S_CONVERT_SAT_F32: Convert {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.convertNumeric(Opcodes.NUMERIC_I64S_CONVERT_SAT_F32)
    }
  }
  object NUMERIC_I64U_CONVERT_SAT_F32: Convert {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.convertNumeric(Opcodes.NUMERIC_I64U_CONVERT_SAT_F32)
    }
  }
  object NUMERIC_I64S_CONVERT_SAT_F64: Convert {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.convertNumeric(Opcodes.NUMERIC_I64S_CONVERT_SAT_F64)
    }
  }
  object NUMERIC_I64U_CONVERT_SAT_F64: Convert {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.convertNumeric(Opcodes.NUMERIC_I64U_CONVERT_SAT_F64)
    }
  }
}
