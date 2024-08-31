package pw.binom.wasm.nodes.inst

import pw.binom.wasm.Opcodes
import pw.binom.wasm.visitors.ExpressionsVisitor

sealed interface Compare : Inst {
  object I32_EQZ : Compare {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.compare(Opcodes.I32_EQZ)
    }
  }

  object I32_EQ : Compare {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.compare(Opcodes.I32_EQ)
    }
  }
  object I32_NE : Compare {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.compare(Opcodes.I32_NE)
    }
  }
  object I32_LT_S : Compare {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.compare(Opcodes.I32_LT_S)
    }
  }
  object I32_LT_U : Compare {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.compare(Opcodes.I32_LT_U)
    }
  }
  object I32_GT_S : Compare {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.compare(Opcodes.I32_GT_S)
    }
  }
  object I32_GT_U : Compare {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.compare(Opcodes.I32_GT_U)
    }
  }
  object I32_LE_S : Compare {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.compare(Opcodes.I32_LE_S)
    }
  }
  object I32_LE_U : Compare {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.compare(Opcodes.I32_LE_U)
    }
  }
  object I32_GE_S : Compare {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.compare(Opcodes.I32_GE_S)
    }
  }
  object I32_GE_U : Compare {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.compare(Opcodes.I32_GE_U)
    }
  }
  object I64_EQZ : Compare {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.compare(Opcodes.I64_EQZ)
    }
  }
  object I64_EQ : Compare {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.compare(Opcodes.I64_EQ)
    }
  }
  object I64_NE : Compare {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.compare(Opcodes.I64_NE)
    }
  }
  object I64_LT_S : Compare {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.compare(Opcodes.I64_LT_S)
    }
  }
  object I64_LT_U : Compare {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.compare(Opcodes.I64_LT_U)
    }
  }
  object I64_GT_S : Compare {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.compare(Opcodes.I64_GT_S)
    }
  }
  object I64_GT_U : Compare {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.compare(Opcodes.I64_GT_U)
    }
  }
  object I64_LE_S : Compare {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.compare(Opcodes.I64_LE_S)
    }
  }
  object I64_LE_U : Compare {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.compare(Opcodes.I64_LE_U)
    }
  }
  object I64_GE_S : Compare {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.compare(Opcodes.I64_GE_S)
    }
  }
  object I64_GE_U : Compare {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.compare(Opcodes.I64_GE_U)
    }
  }
  object F32_EQ : Compare {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.compare(Opcodes.F32_EQ)
    }
  }
  object F32_NE : Compare {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.compare(Opcodes.F32_NE)
    }
  }
  object F32_LT : Compare {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.compare(Opcodes.F32_LT)
    }
  }
  object F32_GT : Compare {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.compare(Opcodes.F32_GT)
    }
  }
  object F32_LE : Compare {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.compare(Opcodes.F32_LE)
    }
  }
  object F32_GE : Compare {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.compare(Opcodes.F32_GE)
    }
  }
  object F64_EQ : Compare {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.compare(Opcodes.F64_EQ)
    }
  }
  object F64_NE : Compare {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.compare(Opcodes.F64_NE)
    }
  }
  object F64_LT : Compare {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.compare(Opcodes.F64_LT)
    }
  }
  object F64_GT : Compare {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.compare(Opcodes.F64_GT)
    }
  }
  object F64_LE : Compare {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.compare(Opcodes.F64_LE)
    }
  }
  object F64_GE : Compare {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.compare(Opcodes.F64_GE)
    }
  }
  object REF_EQ : Compare {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.compare(Opcodes.REF_EQ)
    }
  }
}
