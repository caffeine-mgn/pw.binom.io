package pw.binom.wasm.node.inst

import pw.binom.wasm.Opcodes
import pw.binom.wasm.visitors.ExpressionsVisitor

sealed class Compare : Inst() {
  class I32_EQZ : Compare() {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.compare(Opcodes.I32_EQZ)
    }
  }

  class I32_EQ : Compare() {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.compare(Opcodes.I32_EQ)
    }
  }
  class I32_NE : Compare() {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.compare(Opcodes.I32_NE)
    }
  }
  class I32_LT_S : Compare() {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.compare(Opcodes.I32_LT_S)
    }
  }
  class I32_LT_U : Compare() {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.compare(Opcodes.I32_LT_U)
    }
  }
  class I32_GT_S : Compare() {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.compare(Opcodes.I32_GT_S)
    }
  }
  class I32_GT_U : Compare() {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.compare(Opcodes.I32_GT_U)
    }
  }
  class I32_LE_S : Compare() {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.compare(Opcodes.I32_LE_S)
    }
  }
  class I32_LE_U : Compare() {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.compare(Opcodes.I32_LE_U)
    }
  }
  class I32_GE_S : Compare() {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.compare(Opcodes.I32_GE_S)
    }
  }
  class I32_GE_U : Compare() {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.compare(Opcodes.I32_GE_U)
    }
  }
  class I64_EQZ : Compare() {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.compare(Opcodes.I64_EQZ)
    }
  }
  class I64_EQ : Compare() {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.compare(Opcodes.I64_EQ)
    }
  }
  class I64_NE : Compare() {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.compare(Opcodes.I64_NE)
    }
  }
  class I64_LT_S : Compare() {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.compare(Opcodes.I64_LT_S)
    }
  }
  class I64_LT_U : Compare() {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.compare(Opcodes.I64_LT_U)
    }
  }
  class I64_GT_S : Compare() {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.compare(Opcodes.I64_GT_S)
    }
  }
  class I64_GT_U : Compare() {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.compare(Opcodes.I64_GT_U)
    }
  }
  class I64_LE_S : Compare() {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.compare(Opcodes.I64_LE_S)
    }
  }
  class I64_LE_U : Compare() {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.compare(Opcodes.I64_LE_U)
    }
  }
  class I64_GE_S : Compare() {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.compare(Opcodes.I64_GE_S)
    }
  }
  class I64_GE_U : Compare() {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.compare(Opcodes.I64_GE_U)
    }
  }
  class F32_EQ : Compare() {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.compare(Opcodes.F32_EQ)
    }
  }
  class F32_NE : Compare() {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.compare(Opcodes.F32_NE)
    }
  }
  class F32_LT : Compare() {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.compare(Opcodes.F32_LT)
    }
  }
  class F32_GT : Compare() {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.compare(Opcodes.F32_GT)
    }
  }
  class F32_LE : Compare() {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.compare(Opcodes.F32_LE)
    }
  }
  class F32_GE : Compare() {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.compare(Opcodes.F32_GE)
    }
  }
  class F64_EQ : Compare() {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.compare(Opcodes.F64_EQ)
    }
  }
  class F64_NE : Compare() {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.compare(Opcodes.F64_NE)
    }
  }
  class F64_LT : Compare() {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.compare(Opcodes.F64_LT)
    }
  }
  class F64_GT : Compare() {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.compare(Opcodes.F64_GT)
    }
  }
  class F64_LE : Compare() {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.compare(Opcodes.F64_LE)
    }
  }
  class F64_GE : Compare() {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.compare(Opcodes.F64_GE)
    }
  }
  class REF_EQ : Compare() {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.compare(Opcodes.REF_EQ)
    }
  }
}
