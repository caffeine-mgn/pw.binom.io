package pw.binom.wasm.visitors

import pw.binom.wasm.DataId
import pw.binom.wasm.FieldId
import pw.binom.wasm.FunctionId
import pw.binom.wasm.GlobalId
import pw.binom.wasm.LabelId
import pw.binom.wasm.LocalId
import pw.binom.wasm.TableId
import pw.binom.wasm.TagId
import pw.binom.wasm.TypeId

interface ExpressionsVisitor {
  companion object {
    val SKIP = object : ExpressionsVisitor {}
  }

  interface BrOnCastFailVisitor {
    companion object {
      val SKIP = object : BrOnCastFailVisitor {}
    }

    fun start() {}
    fun end() {}
    fun source(): ValueVisitor.HeapVisitor = ValueVisitor.HeapVisitor.SKIP
    fun target(): ValueVisitor.HeapVisitor = ValueVisitor.HeapVisitor.SKIP
  }

  interface BrTableVisitor {
    companion object {
      val SKIP = object : BrTableVisitor {}
    }

    fun start() {}
    fun end() {}
    fun target(label: LabelId) {}
    fun default(label: LabelId) {}
  }

  interface BlockStartVisitor {
    companion object {
      val SKIP = object : BlockStartVisitor {}
    }

    fun start() {}
    fun withoutType() {}
    fun valueType(): ValueVisitor = ValueVisitor.SKIP
    fun end() {}
  }

  fun start() {}
  fun end() {}

  fun const(value: Float) {}
  fun const(value: Double) {}
  fun const(value: Int) {}
  fun const(value: Long) {}
  fun controlFlow(opcode: UByte) {}

  fun br(opcode: UByte, label: LabelId) {}
  fun endBlock() {}
  fun memory(opcode: UByte, align: UInt, offset: UInt) {}
  fun numeric(opcode: UByte) {}
  fun compare(opcode: UByte) {}
  fun convert(opcode: UByte) {}
  fun structOp(gcOpcode: UByte, type: TypeId, field: FieldId) {}
  fun refNull(): ValueVisitor.HeapVisitor = ValueVisitor.HeapVisitor.SKIP
  fun call(opcode: UByte, function: FunctionId) {}
  fun call(opcode: UByte, type: TypeId, table: TableId) {}
  fun call(opcode: UByte, typeRef: TypeId) {}
  fun indexArgument(opcode: UByte, label: LocalId) {}
  fun indexArgument(opcode: UByte, label: GlobalId) {}
  fun reinterpret(opcode: UByte) {}
  fun arrayOp(gcOpcode: UByte, type: TypeId) {}
  fun ref(gcOpcode: UByte): ValueVisitor.HeapVisitor = ValueVisitor.HeapVisitor.SKIP
  fun structNew(type: TypeId) {}
  fun arrayCopy(from: TypeId, to: TypeId) {}
  fun newArray(type: TypeId, size: UInt) {}
  fun newArray(type: TypeId, data: DataId) {}
  fun brOnCastFail(flags: UByte, label: LabelId): BrOnCastFailVisitor = BrOnCastFailVisitor.SKIP
  fun arrayFull(type: TypeId) {}
  fun newArrayDefault(type: TypeId) {}
  fun arrayLen() {}
  fun ref(function: FunctionId) {}
  fun drop() {}
  fun brTable(): BrTableVisitor = BrTableVisitor.SKIP
  fun select() {}
  fun select(typeCount: Int): ValueVisitor = ValueVisitor.SKIP
  fun throwOp(tag: TagId) {}
  fun startBlock(opcode: UByte): BlockStartVisitor = BlockStartVisitor.SKIP
  fun refIsNull() {}
  fun refAsNonNull() {}
  fun throwRef() {}
  fun catchAll() {}
  fun memorySize(size: UInt) {}
  fun memoryGrow(size: UInt) {}
  fun rethrow(v32u: UInt) {}
  fun catch(v32u: UInt) {}
  fun convertNumeric(opcode: UByte) {}
  fun gcConvert(opcode: UByte) {}
}
