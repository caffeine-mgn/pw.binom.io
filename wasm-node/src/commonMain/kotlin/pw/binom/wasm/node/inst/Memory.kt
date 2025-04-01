package pw.binom.wasm.node.inst

import pw.binom.wasm.MemoryId
import pw.binom.wasm.Opcodes
import pw.binom.wasm.visitors.ExpressionsVisitor

sealed class Memory : Inst() {
  abstract val align: UInt
  abstract val offset: UInt
  abstract val memoryId: MemoryId
  abstract val opcode: UByte
  protected abstract val opName:String

  override fun toString(): String {
    return "$opName align=$align offset=$offset memoryId=$memoryId"
  }

  override fun accept(visitor: ExpressionsVisitor) {
    visitor.memory(
      opcode = opcode,
      align = align,
      offset = offset,
      memoryId = memoryId,
    )
  }

  class I32_LOAD(
    override val align: UInt,
    override val offset: UInt,
    override val memoryId: MemoryId,
  ) : Memory() {
    override val opcode: UByte
      get() = Opcodes.I32_LOAD
    override val opName: String
      get() = "i32.load"
  }

  data class I64_LOAD(
    override val align: UInt,
    override val offset: UInt,
    override val memoryId: MemoryId,
  ) : Memory() {
    override val opcode: UByte
      get() = Opcodes.I64_LOAD
    override val opName: String
      get() = "i64.load"
  }

  data class F32_LOAD(
    override val align: UInt,
    override val offset: UInt,
    override val memoryId: MemoryId,
  ) : Memory() {
    override val opcode: UByte
      get() = Opcodes.F32_LOAD
    override val opName: String
      get() = "f32.load"
  }

  data class F64_LOAD(
    override val align: UInt,
    override val offset: UInt,
    override val memoryId: MemoryId,
  ) : Memory() {
    override val opcode: UByte
      get() = Opcodes.F64_LOAD
    override val opName: String
      get() = "f64.load"
  }

  data class I32_LOAD8_S(
    override val align: UInt,
    override val offset: UInt,
    override val memoryId: MemoryId,
  ) : Memory() {
    override val opcode: UByte
      get() = Opcodes.I32_LOAD8_S
    override val opName: String
      get() = "i32.load8_s"
  }

  data class I32_LOAD8_U(
    override val align: UInt,
    override val offset: UInt,
    override val memoryId: MemoryId,
  ) : Memory() {
    override val opcode: UByte
      get() = Opcodes.I32_LOAD8_U
    override val opName: String
      get() = "i32.load8_u"
  }

  data class I32_LOAD16_S(
    override val align: UInt,
    override val offset: UInt,
    override val memoryId: MemoryId,
  ) : Memory() {
    override val opcode: UByte
      get() = Opcodes.I32_LOAD16_S
    override val opName: String
      get() = "i32.load16_s"
  }

  data class I32_LOAD16_U(
    override val align: UInt,
    override val offset: UInt,
    override val memoryId: MemoryId,
  ) : Memory() {
    override val opcode: UByte
      get() = Opcodes.I32_LOAD16_U
    override val opName: String
      get() = "i32.load16_u"
  }

  data class I64_LOAD8_S(
    override val align: UInt,
    override val offset: UInt,
    override val memoryId: MemoryId,
  ) : Memory() {
    override val opcode: UByte
      get() = Opcodes.I64_LOAD8_S
    override val opName: String
      get() = "i64.load8_s"
  }

  data class I64_LOAD8_U(
    override val align: UInt,
    override val offset: UInt,
    override val memoryId: MemoryId,
  ) : Memory() {
    override val opcode: UByte
      get() = Opcodes.I64_LOAD8_U
    override val opName: String
      get() = "i64.load8_u"
  }

  data class I64_LOAD16_S(
    override val align: UInt,
    override val offset: UInt,
    override val memoryId: MemoryId,
  ) : Memory() {
    override val opcode: UByte
      get() = Opcodes.I64_LOAD16_S
    override val opName: String
      get() = "i64.load16_s"
  }

  data class I64_LOAD16_U(
    override val align: UInt,
    override val offset: UInt,
    override val memoryId: MemoryId,
  ) : Memory() {
    override val opcode: UByte
      get() = Opcodes.I64_LOAD16_U
    override val opName: String
      get() = "i64.load16_u"
  }

  data class I64_LOAD32_S(
    override val align: UInt,
    override val offset: UInt,
    override val memoryId: MemoryId,
  ) : Memory() {
    override val opcode: UByte
      get() = Opcodes.I64_LOAD32_S
    override val opName: String
      get() = "i64.load32_s"
  }

  data class I64_LOAD32_U(
    override val align: UInt,
    override val offset: UInt,
    override val memoryId: MemoryId,
  ) : Memory() {
    override val opcode: UByte
      get() = Opcodes.I64_LOAD32_U
    override val opName: String
      get() = "i64.load32_u"
  }

  data class I32_STORE(
    override val align: UInt,
    override val offset: UInt,
    override val memoryId: MemoryId,
  ) : Memory() {
    override val opcode: UByte
      get() = Opcodes.I32_STORE
    override val opName: String
      get() = "i32.store"
  }

  data class I64_STORE(
    override val align: UInt,
    override val offset: UInt,
    override val memoryId: MemoryId,
  ) : Memory() {
    override val opcode: UByte
      get() = Opcodes.I64_STORE
    override val opName: String
      get() = "i64.store"
  }

  data class F32_STORE(
    override val align: UInt,
    override val offset: UInt,
    override val memoryId: MemoryId,
  ) : Memory() {
    override val opcode: UByte
      get() = Opcodes.F32_STORE
    override val opName: String
      get() = "f32.store"
  }

  data class F64_STORE(
    override val align: UInt,
    override val offset: UInt,
    override val memoryId: MemoryId,
  ) : Memory() {
    override val opcode: UByte
      get() = Opcodes.F64_STORE
    override val opName: String
      get() = "f64.store"
  }

  data class I32_STORE8(
    override val align: UInt,
    override val offset: UInt,
    override val memoryId: MemoryId,
  ) : Memory() {
    override val opcode: UByte
      get() = Opcodes.I32_STORE8
    override val opName: String
      get() = "i32.store8"
  }

  data class I32_STORE16(
    override val align: UInt,
    override val offset: UInt,
    override val memoryId: MemoryId,
  ) : Memory() {
    override val opcode: UByte
      get() = Opcodes.I32_STORE16
    override val opName: String
      get() = "i32.store16"
  }

  data class I64_STORE8(
    override val align: UInt,
    override val offset: UInt,
    override val memoryId: MemoryId,
  ) : Memory() {
    override val opcode: UByte
      get() = Opcodes.I64_STORE8
    override val opName: String
      get() = "i64.store8"
  }

  data class I64_STORE16(
    override val align: UInt,
    override val offset: UInt,
    override val memoryId: MemoryId,
  ) : Memory() {
    override val opcode: UByte
      get() = Opcodes.I64_STORE16
    override val opName: String
      get() = "i64.store16"
  }

  data class I64_STORE32(
    override val align: UInt,
    override val offset: UInt,
    override val memoryId: MemoryId,
  ) : Memory() {
    override val opcode: UByte
      get() = Opcodes.I64_STORE32
    override val opName: String
      get() = "i64.store32"
  }
}
