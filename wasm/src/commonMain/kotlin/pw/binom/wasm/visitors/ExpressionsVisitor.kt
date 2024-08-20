package pw.binom.wasm.visitors

import pw.binom.wasm.DataId
import pw.binom.wasm.FieldId
import pw.binom.wasm.FunctionId
import pw.binom.wasm.GlobalId
import pw.binom.wasm.LabelId
import pw.binom.wasm.LocalId
import pw.binom.wasm.TableId
import pw.binom.wasm.TypeId
import pw.binom.wasm.Types

interface ExpressionsVisitor {
  companion object {
    val STUB = object : ExpressionsVisitor {}
  }

  interface BrOnCastFailVisitor {
    companion object {
      val STUB = object : BrOnCastFailVisitor {}
    }

    fun start() {}
    fun end() {}
    fun source(): ValueVisitor.HeapVisitor = ValueVisitor.HeapVisitor.EMPTY
    fun target(): ValueVisitor.HeapVisitor = ValueVisitor.HeapVisitor.EMPTY
  }

  fun start() {}
  fun end() {}

  fun const(value: Float) {}
  fun const(value: Double) {}
  fun const(value: Int) {}
  fun const(value: Long) {}

  fun br(opcode: UByte, label: LabelId) {}
  fun endBlock() {}
  fun memory(opcode: UByte, align: UInt, offset: UInt) {}
  fun numeric(opcode: UByte) {}
  fun compare(opcode: UByte) {}
  fun convert(opcode: UByte) {}
  fun structOp(opcode: UByte, type: TypeId, field: FieldId) {}
  fun refNull(type: Types) {}
  fun refNull(): ValueVisitor.HeapVisitor = ValueVisitor.HeapVisitor.EMPTY
  fun call(opcode: UByte, function: FunctionId) {}
  fun call(opcode: UByte, type: TypeId, table: TableId) {}
  fun call(opcode: UByte, typeRef: TypeId) {}
  fun indexArgument(opcode: UByte, label: LocalId) {}
  fun indexArgument(opcode: UByte, label: GlobalId) {}
  fun reinterpret(opcode: UByte) {}
  fun arrayOp(opcode: UByte, type: TypeId) {}
  fun ref(opcode: UByte): ValueVisitor.HeapVisitor = ValueVisitor.HeapVisitor.EMPTY
  fun structNew(type: TypeId) = {}
  fun arrayCopy(from: TypeId, to: TypeId) {}
  fun newArray(type: TypeId, size: UInt) {}
  fun newArray(type: TypeId, data: DataId) {}
  fun brOnCastFail(flags: UByte, label: LabelId): BrOnCastFailVisitor = BrOnCastFailVisitor.STUB
  fun arrayFull(type: TypeId) {}
  fun newArrayDefault(type: TypeId) {}
  fun arrayLen() {}
}
