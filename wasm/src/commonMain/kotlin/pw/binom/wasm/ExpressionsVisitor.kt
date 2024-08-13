package pw.binom.wasm

interface ExpressionsVisitor {
  companion object {
    val STUB = object : ExpressionsVisitor {}
  }

  fun const(value: Float) {}
  fun const(value: Double) {}
  fun const(value: Int) {}
  fun const(value: Long) {}

  fun start() {}
  fun end() {}
  fun memory(opcode: UByte, align: UInt, offset: UInt) {}
  fun numeric(opcode: UByte) {}
  fun br(opcode: UByte, label: LabelId) {}
  fun compare(opcode: UByte) {}
  fun convert(opcode: UByte) {}
  fun struct(opcode: UByte, type: TypeId, field: FieldId) {}
  fun call(opcode: UByte, function: Any) {}
}
