package pw.binom.wasm.dom.inst

import pw.binom.wasm.Opcodes
import pw.binom.wasm.visitors.ExpressionsVisitor

sealed interface Memory2 : Inst {
  val align: UInt
  val offset: UInt

  class I32_LOAD(override val align: UInt, override val offset: UInt) : Memory2 {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.memory(
        opcode = Opcodes.I32_LOAD,
        align = align,
        offset = offset,
      )
    }
  }

  class I64_LOAD(override val align: UInt, override val offset: UInt) : Memory2 {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.memory(
        opcode = Opcodes.I32_LOAD,
        align = align,
        offset = offset,
      )
    }
  }

  class F32_LOAD(override val align: UInt, override val offset: UInt) : Memory2 {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.memory(
        opcode = Opcodes.I32_LOAD,
        align = align,
        offset = offset,
      )
    }
  }

  class F64_LOAD(override val align: UInt, override val offset: UInt) : Memory2 {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.memory(
        opcode = Opcodes.I32_LOAD,
        align = align,
        offset = offset,
      )
    }
  }

  class I32_LOAD8_S(override val align: UInt, override val offset: UInt) : Memory2 {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.memory(
        opcode = Opcodes.I32_LOAD,
        align = align,
        offset = offset,
      )
    }
  }

  class I32_LOAD8_U(override val align: UInt, override val offset: UInt) : Memory2 {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.memory(
        opcode = Opcodes.I32_LOAD,
        align = align,
        offset = offset,
      )
    }
  }

  class I32_LOAD16_S(override val align: UInt, override val offset: UInt) : Memory2 {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.memory(
        opcode = Opcodes.I32_LOAD,
        align = align,
        offset = offset,
      )
    }
  }

  class I32_LOAD16_U(override val align: UInt, override val offset: UInt) : Memory2 {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.memory(
        opcode = Opcodes.I32_LOAD,
        align = align,
        offset = offset,
      )
    }
  }

  class I64_LOAD8_S(override val align: UInt, override val offset: UInt) : Memory2 {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.memory(
        opcode = Opcodes.I32_LOAD,
        align = align,
        offset = offset,
      )
    }
  }

  class I64_LOAD8_U(override val align: UInt, override val offset: UInt) : Memory2 {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.memory(
        opcode = Opcodes.I32_LOAD,
        align = align,
        offset = offset,
      )
    }
  }

  class I64_LOAD16_S(override val align: UInt, override val offset: UInt) : Memory2 {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.memory(
        opcode = Opcodes.I32_LOAD,
        align = align,
        offset = offset,
      )
    }
  }

  class I64_LOAD16_U(override val align: UInt, override val offset: UInt) : Memory2 {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.memory(
        opcode = Opcodes.I32_LOAD,
        align = align,
        offset = offset,
      )
    }
  }

  class I64_LOAD32_S(override val align: UInt, override val offset: UInt) : Memory2 {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.memory(
        opcode = Opcodes.I32_LOAD,
        align = align,
        offset = offset,
      )
    }
  }

  class I64_LOAD32_U(override val align: UInt, override val offset: UInt) : Memory2 {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.memory(
        opcode = Opcodes.I32_LOAD,
        align = align,
        offset = offset,
      )
    }
  }

  class I32_STORE(override val align: UInt, override val offset: UInt) : Memory2 {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.memory(
        opcode = Opcodes.I32_LOAD,
        align = align,
        offset = offset,
      )
    }
  }

  class I64_STORE(override val align: UInt, override val offset: UInt) : Memory2 {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.memory(
        opcode = Opcodes.I32_LOAD,
        align = align,
        offset = offset,
      )
    }
  }

  class F32_STORE(override val align: UInt, override val offset: UInt) : Memory2 {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.memory(
        opcode = Opcodes.I32_LOAD,
        align = align,
        offset = offset,
      )
    }
  }

  class F64_STORE(override val align: UInt, override val offset: UInt) : Memory2 {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.memory(
        opcode = Opcodes.I32_LOAD,
        align = align,
        offset = offset,
      )
    }
  }

  class I32_STORE8(override val align: UInt, override val offset: UInt) : Memory2 {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.memory(
        opcode = Opcodes.I32_LOAD,
        align = align,
        offset = offset,
      )
    }
  }

  class I32_STORE16(override val align: UInt, override val offset: UInt) : Memory2 {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.memory(
        opcode = Opcodes.I32_LOAD,
        align = align,
        offset = offset,
      )
    }
  }

  class I64_STORE8(override val align: UInt, override val offset: UInt) : Memory2 {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.memory(
        opcode = Opcodes.I32_LOAD,
        align = align,
        offset = offset,
      )
    }
  }

  class I64_STORE16(override val align: UInt, override val offset: UInt) : Memory2 {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.memory(
        opcode = Opcodes.I32_LOAD,
        align = align,
        offset = offset,
      )
    }
  }

  class I64_STORE32(override val align: UInt, override val offset: UInt) : Memory2 {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.memory(
        opcode = Opcodes.I32_LOAD,
        align = align,
        offset = offset,
      )
    }
  }
}
