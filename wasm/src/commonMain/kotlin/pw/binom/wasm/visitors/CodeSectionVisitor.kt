package pw.binom.wasm.visitors

import pw.binom.wasm.ValueType

interface CodeSectionVisitor {
  companion object {
    val STUB = object : CodeSectionVisitor {}
  }

  fun start(size: UInt) {}
  fun end() {}
  fun indexArgument(opcode: UByte, index: Int) {}
  fun memOpAlignOffsetArg(opcode: UByte, readVarUInt32AsInt: Int, readVarUInt32: Long) {}
  fun numOp(opcode: UByte) {}
  fun controlFlow(opcode: UByte, type: ValueType?) {}
  fun controlFlow(opcode: UByte, labelIndex: Int) {}
  fun controlFlow(opcode: UByte) {}
  fun const(opcode: UByte, fromBits: Float) {}
  fun const(opcode: UByte, value: Int) {}
  fun compare(opcode: UByte) {}
  fun convert(opcode: UByte) {}
  fun local(type: ValueType) {}
}
