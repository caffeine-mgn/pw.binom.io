package pw.binom.wasm.node.inst

import pw.binom.wasm.Opcodes
import pw.binom.wasm.visitors.ExpressionsVisitor

sealed class Convert : Inst() {
  class I32_WRAP_I64 : Convert() {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.convert(Opcodes.I32_WRAP_I64)
    }
  }

  class I32_TRUNC_S_F32 : Convert() {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.convert(Opcodes.I32_TRUNC_S_F32)
    }
  }

  class I32_TRUNC_U_F32 : Convert() {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.convert(Opcodes.I32_TRUNC_U_F32)
    }
  }

  class I32_TRUNC_S_F64 : Convert() {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.convert(Opcodes.I32_TRUNC_S_F64)
    }
  }

  class I32_TRUNC_U_F64 : Convert() {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.convert(Opcodes.I32_TRUNC_U_F64)
    }
  }

  class I64_EXTEND_S_I32 : Convert() {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.convert(Opcodes.I64_EXTEND_S_I32)
    }
  }

  class I64_EXTEND_U_I32 : Convert() {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.convert(Opcodes.I64_EXTEND_U_I32)
    }
  }

  class I64_TRUNC_S_F32 : Convert() {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.convert(Opcodes.I64_TRUNC_S_F32)
    }
  }

  class I64_TRUNC_U_F32 : Convert() {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.convert(Opcodes.I64_TRUNC_U_F32)
    }
  }

  class I64_TRUNC_S_F64 : Convert() {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.convert(Opcodes.I64_TRUNC_S_F64)
    }
  }

  class I64_TRUNC_U_F64 : Convert() {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.convert(Opcodes.I64_TRUNC_U_F64)
    }
  }

  class F32_CONVERT_S_I32 : Convert() {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.convert(Opcodes.F32_CONVERT_S_I32)
    }
  }

  class F32_CONVERT_U_I32 : Convert() {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.convert(Opcodes.F32_CONVERT_U_I32)
    }
  }

  class F32_CONVERT_S_I64 : Convert() {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.convert(Opcodes.F32_CONVERT_S_I64)
    }
  }

  class F32_CONVERT_U_I64 : Convert() {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.convert(Opcodes.F32_CONVERT_U_I64)
    }
  }

  class F32_DEMOTE_F64 : Convert() {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.convert(Opcodes.F32_DEMOTE_F64)
    }
  }

  class F64_CONVERT_S_I32 : Convert() {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.convert(Opcodes.F64_CONVERT_S_I32)
    }
  }

  class F64_CONVERT_U_I32 : Convert() {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.convert(Opcodes.F64_CONVERT_U_I32)
    }
  }

  class F64_CONVERT_S_I64 : Convert() {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.convert(Opcodes.F64_CONVERT_S_I64)
    }
  }

  class F64_CONVERT_U_I64 : Convert() {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.convert(Opcodes.F64_CONVERT_U_I64)
    }
  }

  class F64_PROMOTE_F32 : Convert() {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.convert(Opcodes.F64_PROMOTE_F32)
    }
  }

  class GC_ANY_CONVERT_EXTERN : Convert() {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.gcConvert(Opcodes.GC_ANY_CONVERT_EXTERN)
    }
  }

  class GC_EXTERN_CONVERT_ANY : Convert() {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.gcConvert(Opcodes.GC_EXTERN_CONVERT_ANY)
    }
  }

  class NUMERIC_I32S_CONVERT_SAT_F32: Convert() {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.convertNumeric(Opcodes.NUMERIC_I32S_CONVERT_SAT_F32)
    }
  }
  class NUMERIC_I32U_CONVERT_SAT_F32: Convert() {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.convertNumeric(Opcodes.NUMERIC_I32U_CONVERT_SAT_F32)
    }
  }
  class NUMERIC_I32S_CONVERT_SAT_F64: Convert() {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.convertNumeric(Opcodes.NUMERIC_I32S_CONVERT_SAT_F64)
    }
  }
  class NUMERIC_I32U_CONVERT_SAT_F64: Convert() {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.convertNumeric(Opcodes.NUMERIC_I32U_CONVERT_SAT_F64)
    }
  }
  class NUMERIC_I64S_CONVERT_SAT_F32: Convert() {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.convertNumeric(Opcodes.NUMERIC_I64S_CONVERT_SAT_F32)
    }
  }
  class NUMERIC_I64U_CONVERT_SAT_F32: Convert() {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.convertNumeric(Opcodes.NUMERIC_I64U_CONVERT_SAT_F32)
    }
  }
  class NUMERIC_I64S_CONVERT_SAT_F64: Convert() {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.convertNumeric(Opcodes.NUMERIC_I64S_CONVERT_SAT_F64)
    }
  }
  class NUMERIC_I64U_CONVERT_SAT_F64: Convert() {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.convertNumeric(Opcodes.NUMERIC_I64U_CONVERT_SAT_F64)
    }
  }
}
