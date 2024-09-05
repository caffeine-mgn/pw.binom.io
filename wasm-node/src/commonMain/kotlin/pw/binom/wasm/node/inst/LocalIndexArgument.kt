package pw.binom.wasm.node.inst

import pw.binom.wasm.LocalId
import pw.binom.wasm.Opcodes
import pw.binom.wasm.visitors.ExpressionsVisitor

sealed interface LocalIndexArgument : Inst {
  val id: LocalId

  data class GET(override val id: LocalId) : LocalIndexArgument {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.indexArgument(opcode = Opcodes.GET_LOCAL, value = id)
    }
  }

  data class SET(override val id: LocalId) : LocalIndexArgument {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.indexArgument(opcode = Opcodes.SET_LOCAL, value = id)
    }
  }

  data class TEE(override val id: LocalId) : LocalIndexArgument {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.indexArgument(opcode = Opcodes.TEE_LOCAL, value = id)
    }
  }
}
