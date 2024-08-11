package pw.binom.wasm

interface CodeSectionVisitor {
  fun start(size:Int)
  fun end()
  fun indexArgument(opcode:UByte,index: Int)
  fun memOpAlignOffsetArg(opcode: UByte, readVarUInt32AsInt: Int, readVarUInt32: Long)
  fun numOp(opcode: UByte)
  fun controlFlow(opcode: UByte, type: ValueType?)
  fun controlFlow(opcode: UByte, labelIndex: Int)
  fun controlFlow(opcode: UByte)
  fun const(opcode: UByte, fromBits: Float)
  fun const(opcode: UByte, value: Int)
  fun compare(opcode: UByte)
  fun convert(opcode: UByte)
  fun local(type: ValueType)
}
