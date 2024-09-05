package pw.binom.wasm.node.inst

import pw.binom.wasm.GlobalId
import pw.binom.wasm.Opcodes
import pw.binom.wasm.visitors.ExpressionsVisitor

sealed interface GlobalIndexArgument : Inst {
  val id: GlobalId

  data class GET(override val id: GlobalId) : GlobalIndexArgument {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.indexArgument(opcode = Opcodes.GET_GLOBAL, value = id)
    }
  }

  data class SET(override val id: GlobalId) : GlobalIndexArgument {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.indexArgument(opcode = Opcodes.SET_GLOBAL, value = id)
    }
  }
}
