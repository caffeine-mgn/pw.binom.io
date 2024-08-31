package pw.binom.wasm.dom.inst

import pw.binom.wasm.LabelId
import pw.binom.wasm.Opcodes
import pw.binom.wasm.visitors.ExpressionsVisitor

sealed interface Br : Inst {
  val label: LabelId

  class BR(override val label: LabelId) : Br {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.br(opcode = Opcodes.BR, label = label)
    }
  }

  class BR_IF(override val label: LabelId) : Br {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.br(opcode = Opcodes.BR_IF, label = label)
    }
  }

  class BR_ON_NULL(override val label: LabelId) : Br {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.br(opcode = Opcodes.BR_ON_NULL, label = label)
    }
  }

  class BR_ON_NON_NULL(override val label: LabelId) : Br {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.br(opcode = Opcodes.BR_ON_NON_NULL, label = label)
    }
  }
}
