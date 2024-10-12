package pw.binom.wasm.node.inst

import pw.binom.wasm.MemoryId
import pw.binom.wasm.Opcodes
import pw.binom.wasm.visitors.ExpressionsVisitor

sealed class Memory : Inst() {
  abstract val align: UInt
  abstract val offset: UInt
  abstract val memoryId: MemoryId
  abstract val opcode: UByte

  override fun accept(visitor: ExpressionsVisitor) {
    visitor.memory(
      opcode = opcode,
      align = align,
      offset = offset,
      memoryId = memoryId,
    )
  }

  data class I32_LOAD(
    override val align: UInt,
    override val offset: UInt,
    override val memoryId: MemoryId,
  ) : Memory() {
    override val opcode: UByte
      get() = Opcodes.I32_LOAD
  }

  data class I64_LOAD(
    override val align: UInt,
    override val offset: UInt,
    override val memoryId: MemoryId,
  ) : Memory() {
    override val opcode: UByte
      get() = Opcodes.I64_LOAD
  }

  data class F32_LOAD(
    override val align: UInt,
    override val offset: UInt,
    override val memoryId: MemoryId,
  ) : Memory() {
    override val opcode: UByte
      get() = Opcodes.F32_LOAD
  }

  data class F64_LOAD(
    override val align: UInt,
    override val offset: UInt,
    override val memoryId: MemoryId,
  ) : Memory() {
    override val opcode: UByte
      get() = Opcodes.F64_LOAD
  }

  data class I32_LOAD8_S(
    override val align: UInt,
    override val offset: UInt,
    override val memoryId: MemoryId,
  ) : Memory() {
    override val opcode: UByte
      get() = Opcodes.I32_LOAD8_S
  }

  data class I32_LOAD8_U(
    override val align: UInt,
    override val offset: UInt,
    override val memoryId: MemoryId,
  ) : Memory() {
    override val opcode: UByte
      get() = Opcodes.I32_LOAD8_U
  }

  data class I32_LOAD16_S(
    override val align: UInt,
    override val offset: UInt,
    override val memoryId: MemoryId,
  ) : Memory() {
    override val opcode: UByte
      get() = Opcodes.I32_LOAD16_S
  }

  data class I32_LOAD16_U(
    override val align: UInt,
    override val offset: UInt,
    override val memoryId: MemoryId,
  ) : Memory() {
    override val opcode: UByte
      get() = Opcodes.I32_LOAD16_U
  }

  data class I64_LOAD8_S(
    override val align: UInt,
    override val offset: UInt,
    override val memoryId: MemoryId,
  ) : Memory() {
    override val opcode: UByte
      get() = Opcodes.I64_LOAD8_S
  }

  data class I64_LOAD8_U(
    override val align: UInt,
    override val offset: UInt,
    override val memoryId: MemoryId,
  ) : Memory() {
    override val opcode: UByte
      get() = Opcodes.I64_LOAD8_U
  }

  data class I64_LOAD16_S(
    override val align: UInt,
    override val offset: UInt,
    override val memoryId: MemoryId,
  ) : Memory() {
    override val opcode: UByte
      get() = Opcodes.I64_LOAD16_S
  }

  data class I64_LOAD16_U(
    override val align: UInt,
    override val offset: UInt,
    override val memoryId: MemoryId,
  ) : Memory() {
    override val opcode: UByte
      get() = Opcodes.I64_LOAD16_U
  }

  data class I64_LOAD32_S(
    override val align: UInt,
    override val offset: UInt,
    override val memoryId: MemoryId,
  ) : Memory() {
    override val opcode: UByte
      get() = Opcodes.I64_LOAD32_S
  }

  data class I64_LOAD32_U(
    override val align: UInt,
    override val offset: UInt,
    override val memoryId: MemoryId,
  ) : Memory() {
    override val opcode: UByte
      get() = Opcodes.I64_LOAD32_U
  }

  data class I32_STORE(
    override val align: UInt,
    override val offset: UInt,
    override val memoryId: MemoryId,
  ) : Memory() {
    override val opcode: UByte
      get() = Opcodes.I32_STORE
  }

  data class I64_STORE(
    override val align: UInt,
    override val offset: UInt,
    override val memoryId: MemoryId,
  ) : Memory() {
    override val opcode: UByte
      get() = Opcodes.I64_STORE
  }

  data class F32_STORE(
    override val align: UInt,
    override val offset: UInt,
    override val memoryId: MemoryId,
  ) : Memory() {
    override val opcode: UByte
      get() = Opcodes.F32_STORE
  }

  data class F64_STORE(
    override val align: UInt,
    override val offset: UInt,
    override val memoryId: MemoryId,
  ) : Memory() {
    override val opcode: UByte
      get() = Opcodes.F64_STORE
  }

  data class I32_STORE8(
    override val align: UInt,
    override val offset: UInt,
    override val memoryId: MemoryId,
  ) : Memory() {
    override val opcode: UByte
      get() = Opcodes.I32_STORE8
  }

  data class I32_STORE16(
    override val align: UInt,
    override val offset: UInt,
    override val memoryId: MemoryId,
  ) : Memory() {
    override val opcode: UByte
      get() = Opcodes.I32_STORE16
  }

  data class I64_STORE8(
    override val align: UInt,
    override val offset: UInt,
    override val memoryId: MemoryId,
  ) : Memory() {
    override val opcode: UByte
      get() = Opcodes.I64_STORE8
  }

  data class I64_STORE16(
    override val align: UInt,
    override val offset: UInt,
    override val memoryId: MemoryId,
  ) : Memory() {
    override val opcode: UByte
      get() = Opcodes.I64_STORE16
  }

  data class I64_STORE32(
    override val align: UInt,
    override val offset: UInt,
    override val memoryId: MemoryId,
  ) : Memory() {
    override val opcode: UByte
      get() = Opcodes.I64_STORE32
  }
}
