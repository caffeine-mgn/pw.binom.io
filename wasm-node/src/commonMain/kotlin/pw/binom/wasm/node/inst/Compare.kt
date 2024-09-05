package pw.binom.wasm.node.inst

import pw.binom.wasm.Opcodes
import pw.binom.wasm.visitors.ExpressionsVisitor

sealed interface Compare : Inst {
  data object I32_EQZ : Compare {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.compare(Opcodes.I32_EQZ)
    }
  }

  data object I32_EQ : Compare {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.compare(Opcodes.I32_EQ)
    }
  }
  data object I32_NE : Compare {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.compare(Opcodes.I32_NE)
    }
  }
  data object I32_LT_S : Compare {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.compare(Opcodes.I32_LT_S)
    }
  }
  data object I32_LT_U : Compare {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.compare(Opcodes.I32_LT_U)
    }
  }
  data object I32_GT_S : Compare {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.compare(Opcodes.I32_GT_S)
    }
  }
  data object I32_GT_U : Compare {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.compare(Opcodes.I32_GT_U)
    }
  }
  data object I32_LE_S : Compare {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.compare(Opcodes.I32_LE_S)
    }
  }
  data object I32_LE_U : Compare {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.compare(Opcodes.I32_LE_U)
    }
  }
  data object I32_GE_S : Compare {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.compare(Opcodes.I32_GE_S)
    }
  }
  data object I32_GE_U : Compare {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.compare(Opcodes.I32_GE_U)
    }
  }
  data object I64_EQZ : Compare {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.compare(Opcodes.I64_EQZ)
    }
  }
  data object I64_EQ : Compare {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.compare(Opcodes.I64_EQ)
    }
  }
  data object I64_NE : Compare {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.compare(Opcodes.I64_NE)
    }
  }
  data object I64_LT_S : Compare {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.compare(Opcodes.I64_LT_S)
    }
  }
  data object I64_LT_U : Compare {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.compare(Opcodes.I64_LT_U)
    }
  }
  data object I64_GT_S : Compare {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.compare(Opcodes.I64_GT_S)
    }
  }
  data object I64_GT_U : Compare {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.compare(Opcodes.I64_GT_U)
    }
  }
  data object I64_LE_S : Compare {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.compare(Opcodes.I64_LE_S)
    }
  }
  data object I64_LE_U : Compare {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.compare(Opcodes.I64_LE_U)
    }
  }
  data object I64_GE_S : Compare {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.compare(Opcodes.I64_GE_S)
    }
  }
  data object I64_GE_U : Compare {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.compare(Opcodes.I64_GE_U)
    }
  }
  data object F32_EQ : Compare {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.compare(Opcodes.F32_EQ)
    }
  }
  data object F32_NE : Compare {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.compare(Opcodes.F32_NE)
    }
  }
  data object F32_LT : Compare {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.compare(Opcodes.F32_LT)
    }
  }
  data object F32_GT : Compare {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.compare(Opcodes.F32_GT)
    }
  }
  data object F32_LE : Compare {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.compare(Opcodes.F32_LE)
    }
  }
  data object F32_GE : Compare {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.compare(Opcodes.F32_GE)
    }
  }
  data object F64_EQ : Compare {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.compare(Opcodes.F64_EQ)
    }
  }
  data object F64_NE : Compare {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.compare(Opcodes.F64_NE)
    }
  }
  data object F64_LT : Compare {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.compare(Opcodes.F64_LT)
    }
  }
  data object F64_GT : Compare {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.compare(Opcodes.F64_GT)
    }
  }
  data object F64_LE : Compare {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.compare(Opcodes.F64_LE)
    }
  }
  data object F64_GE : Compare {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.compare(Opcodes.F64_GE)
    }
  }
  data object REF_EQ : Compare {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.compare(Opcodes.REF_EQ)
    }
  }
}
