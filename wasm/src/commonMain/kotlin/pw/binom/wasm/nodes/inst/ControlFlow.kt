package pw.binom.wasm.nodes.inst

import pw.binom.wasm.Opcodes
import pw.binom.wasm.visitors.ExpressionsVisitor

interface ControlFlow : Inst {
  object UNREACHABLE : ControlFlow {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.controlFlow(Opcodes.UNREACHABLE)
    }
  }

  object NOP : ControlFlow {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.controlFlow(Opcodes.NOP)
    }
  }

  object ELSE : ControlFlow {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.controlFlow(Opcodes.ELSE)
    }
  }

  object RETURN : ControlFlow {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.controlFlow(Opcodes.RETURN)
    }
  }
}
