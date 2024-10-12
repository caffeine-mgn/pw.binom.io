package pw.binom.wasm.node.inst

import pw.binom.wasm.Opcodes
import pw.binom.wasm.visitors.ExpressionsVisitor

sealed class ControlFlow : Inst() {
  class UNREACHABLE : ControlFlow() {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.controlFlow(Opcodes.UNREACHABLE)
    }
  }

  class NOP : ControlFlow() {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.controlFlow(Opcodes.NOP)
    }
  }

  class ELSE : ControlFlow() {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.controlFlow(Opcodes.ELSE)
    }
  }

  class RETURN : ControlFlow() {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.controlFlow(Opcodes.RETURN)
    }
  }
}
