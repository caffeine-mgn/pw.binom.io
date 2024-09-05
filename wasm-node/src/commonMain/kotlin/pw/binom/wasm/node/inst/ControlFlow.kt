package pw.binom.wasm.node.inst

import pw.binom.wasm.Opcodes
import pw.binom.wasm.visitors.ExpressionsVisitor

interface ControlFlow : Inst {
  data object UNREACHABLE : ControlFlow {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.controlFlow(Opcodes.UNREACHABLE)
    }
  }

  data object NOP : ControlFlow {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.controlFlow(Opcodes.NOP)
    }
  }

  data object ELSE : ControlFlow {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.controlFlow(Opcodes.ELSE)
    }
  }

  data object RETURN : ControlFlow {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.controlFlow(Opcodes.RETURN)
    }
  }
}
