package pw.binom.wasm.node

import pw.binom.wasm.*
import pw.binom.wasm.visitors.ExpressionsVisitor
import pw.binom.wasm.visitors.ValueVisitor
import pw.binom.wasm.node.inst.MemoryOp
import pw.binom.wasm.node.inst.*

class Expressions : ExpressionsVisitor, MutableList<Inst> by ArrayList() {

  var first: Inst? = null
  var last: Inst? = null

  private fun <T : Inst> T.add(): T {
    if (first == null) {
      first = this
      last = this
    } else {
      last!!.next = this
      last = this
    }
    this@Expressions.add(this)
    return this
  }

  override fun start() {
    clear()
  }

  override fun end() {
    var cmd = first
    val l = ArrayList<Inst>()
    while (cmd != null) {
      l += cmd
      cmd = cmd.next
    }
    if (l.size != size) TODO()
    forEachIndexed { index, inst ->
      if (l[index] !== inst) TODO()
    }
    super.end()
  }

  override fun const(value: Float) {
    F32Const(value).add()
  }

  override fun const(value: Double) {
    F64Const(value).add()
  }

  override fun const(value: Int) {
    I32Const(value).add()
  }

  override fun const(value: Long) {
    I64Const(value).add()
  }

  override fun controlFlow(opcode: UByte) {
    when (opcode) {
      Opcodes.UNREACHABLE -> ControlFlow.UNREACHABLE()
      Opcodes.NOP -> ControlFlow.NOP()
      Opcodes.ELSE -> ControlFlow.ELSE()
      Opcodes.RETURN -> ControlFlow.RETURN()
      else -> throw IllegalArgumentException()
    }.add()
  }

  override fun br(opcode: UByte, label: LabelId) {
    when (opcode) {
      Opcodes.BR -> Br.BR(label)
      Opcodes.BR_IF -> Br.BR_IF(label)
      Opcodes.BR_ON_NULL -> Br.BR_ON_NULL(label)
      Opcodes.BR_ON_NON_NULL -> Br.BR_ON_NON_NULL(label)
      else -> throw IllegalArgumentException()
    }.add()
  }

  override fun endBlock() {
    EndBlock().add()
  }

  override fun memory(opcode: UByte, align: UInt, offset: UInt, memoryId: MemoryId) {
    when (opcode) {
      Opcodes.I32_LOAD -> Memory.I32_LOAD(align = align, offset = offset, memoryId = memoryId)
      Opcodes.I64_LOAD -> Memory.I64_LOAD(align = align, offset = offset, memoryId = memoryId)
      Opcodes.F32_LOAD -> Memory.F32_LOAD(align = align, offset = offset, memoryId = memoryId)
      Opcodes.F64_LOAD -> Memory.F64_LOAD(align = align, offset = offset, memoryId = memoryId)
      Opcodes.I32_LOAD8_S -> Memory.I32_LOAD8_S(align = align, offset = offset, memoryId = memoryId)
      Opcodes.I32_LOAD8_U -> Memory.I32_LOAD8_U(align = align, offset = offset, memoryId = memoryId)
      Opcodes.I32_LOAD16_S -> Memory.I32_LOAD16_S(align = align, offset = offset, memoryId = memoryId)
      Opcodes.I32_LOAD16_U -> Memory.I32_LOAD16_U(align = align, offset = offset, memoryId = memoryId)
      Opcodes.I64_LOAD8_S -> Memory.I64_LOAD8_S(align = align, offset = offset, memoryId = memoryId)
      Opcodes.I64_LOAD8_U -> Memory.I64_LOAD8_U(align = align, offset = offset, memoryId = memoryId)
      Opcodes.I64_LOAD16_S -> Memory.I64_LOAD16_S(align = align, offset = offset, memoryId = memoryId)
      Opcodes.I64_LOAD16_U -> Memory.I64_LOAD16_U(align = align, offset = offset, memoryId = memoryId)
      Opcodes.I64_LOAD32_S -> Memory.I64_LOAD32_S(align = align, offset = offset, memoryId = memoryId)
      Opcodes.I64_LOAD32_U -> Memory.I64_LOAD32_U(align = align, offset = offset, memoryId = memoryId)
      Opcodes.I32_STORE -> Memory.I32_STORE(align = align, offset = offset, memoryId = memoryId)
      Opcodes.I64_STORE -> Memory.I64_STORE(align = align, offset = offset, memoryId = memoryId)
      Opcodes.F32_STORE -> Memory.F32_STORE(align = align, offset = offset, memoryId = memoryId)
      Opcodes.F64_STORE -> Memory.F64_STORE(align = align, offset = offset, memoryId = memoryId)
      Opcodes.I32_STORE8 -> Memory.I32_STORE8(align = align, offset = offset, memoryId = memoryId)
      Opcodes.I32_STORE16 -> Memory.I32_STORE16(align = align, offset = offset, memoryId = memoryId)
      Opcodes.I64_STORE8 -> Memory.I64_STORE8(align = align, offset = offset, memoryId = memoryId)
      Opcodes.I64_STORE16 -> Memory.I64_STORE16(align = align, offset = offset, memoryId = memoryId)
      Opcodes.I64_STORE32 -> Memory.I64_STORE32(align = align, offset = offset, memoryId = memoryId)

      else -> throw IllegalArgumentException()
    }.add()
  }

  override fun numeric(opcode: UByte) {
    when (opcode) {
      Opcodes.I32_CLZ -> Numeric.I32_CLZ()
      Opcodes.I32_CTZ -> Numeric.I32_CTZ()
      Opcodes.I32_POPCNT -> Numeric.I32_POPCNT()
      Opcodes.I32_ADD -> Numeric.I32_ADD()
      Opcodes.I32_SUB -> Numeric.I32_SUB()
      Opcodes.I32_MUL -> Numeric.I32_MUL()
      Opcodes.I32_DIV_S -> Numeric.I32_DIV_S()
      Opcodes.I32_DIV_U -> Numeric.I32_DIV_U()
      Opcodes.I32_REM_S -> Numeric.I32_REM_S()
      Opcodes.I32_REM_U -> Numeric.I32_REM_U()
      Opcodes.I32_AND -> Numeric.I32_AND()
      Opcodes.I32_OR -> Numeric.I32_OR()
      Opcodes.I32_XOR -> Numeric.I32_XOR()
      Opcodes.I32_SHL -> Numeric.I32_SHL()
      Opcodes.I32_SHR_S -> Numeric.I32_SHR_S()
      Opcodes.I32_SHR_U -> Numeric.I32_SHR_U()
      Opcodes.I32_ROTL -> Numeric.I32_ROTL()
      Opcodes.I32_ROTR -> Numeric.I32_ROTR()
      Opcodes.I64_CLZ -> Numeric.I64_CLZ()
      Opcodes.I64_CTZ -> Numeric.I64_CTZ()
      Opcodes.I64_POPCNT -> Numeric.I64_POPCNT()
      Opcodes.I64_ADD -> Numeric.I64_ADD()
      Opcodes.I64_SUB -> Numeric.I64_SUB()
      Opcodes.I64_MUL -> Numeric.I64_MUL()
      Opcodes.I64_DIV_S -> Numeric.I64_DIV_S()
      Opcodes.I64_DIV_U -> Numeric.I64_DIV_U()
      Opcodes.I64_REM_S -> Numeric.I64_REM_S()
      Opcodes.I64_REM_U -> Numeric.I64_REM_U()
      Opcodes.I64_AND -> Numeric.I64_AND()
      Opcodes.I64_OR -> Numeric.I64_OR()
      Opcodes.I64_XOR -> Numeric.I64_XOR()
      Opcodes.I64_SHL -> Numeric.I64_SHL()
      Opcodes.I64_SHR_S -> Numeric.I64_SHR_S()
      Opcodes.I64_SHR_U -> Numeric.I64_SHR_U()
      Opcodes.I64_ROTL -> Numeric.I64_ROTL()
      Opcodes.I64_ROTR -> Numeric.I64_ROTR()
      Opcodes.F32_ABS -> Numeric.F32_ABS()
      Opcodes.F32_NEG -> Numeric.F32_NEG()
      Opcodes.F32_CEIL -> Numeric.F32_CEIL()
      Opcodes.F32_FLOOR -> Numeric.F32_FLOOR()
      Opcodes.F32_TRUNC -> Numeric.F32_TRUNC()
      Opcodes.F32_NEAREST -> Numeric.F32_NEAREST()
      Opcodes.F32_SQRT -> Numeric.F32_SQRT()
      Opcodes.F32_ADD -> Numeric.F32_ADD()
      Opcodes.F32_SUB -> Numeric.F32_SUB()
      Opcodes.F32_MUL -> Numeric.F32_MUL()
      Opcodes.F32_DIV -> Numeric.F32_DIV()
      Opcodes.F32_MIN -> Numeric.F32_MIN()
      Opcodes.F32_MAX -> Numeric.F32_MAX()
      Opcodes.F32_COPYSIGN -> Numeric.F32_COPYSIGN()
      Opcodes.F64_ABS -> Numeric.F64_ABS()
      Opcodes.F64_NEG -> Numeric.F64_NEG()
      Opcodes.F64_CEIL -> Numeric.F64_CEIL()
      Opcodes.F64_FLOOR -> Numeric.F64_FLOOR()
      Opcodes.F64_TRUNC -> Numeric.F64_TRUNC()
      Opcodes.F64_NEAREST -> Numeric.F64_NEAREST()
      Opcodes.F64_SQRT -> Numeric.F64_SQRT()
      Opcodes.F64_ADD -> Numeric.F64_ADD()
      Opcodes.F64_SUB -> Numeric.F64_SUB()
      Opcodes.F64_MUL -> Numeric.F64_MUL()
      Opcodes.F64_DIV -> Numeric.F64_DIV()
      Opcodes.F64_MIN -> Numeric.F64_MIN()
      Opcodes.F64_MAX -> Numeric.F64_MAX()
      Opcodes.F64_COPYSIGN -> Numeric.F64_COPYSIGN()

      else -> throw IllegalArgumentException()
    }.add()
  }

  override fun compare(opcode: UByte) {
    when (opcode) {
      Opcodes.I32_EQZ -> Compare.I32_EQZ()
      Opcodes.I32_EQ -> Compare.I32_EQ()
      Opcodes.I32_NE -> Compare.I32_NE()
      Opcodes.I32_LT_S -> Compare.I32_LT_S()
      Opcodes.I32_LT_U -> Compare.I32_LT_U()
      Opcodes.I32_GT_S -> Compare.I32_GT_S()
      Opcodes.I32_GT_U -> Compare.I32_GT_U()
      Opcodes.I32_LE_S -> Compare.I32_LE_S()
      Opcodes.I32_LE_U -> Compare.I32_LE_U()
      Opcodes.I32_GE_S -> Compare.I32_GE_S()
      Opcodes.I32_GE_U -> Compare.I32_GE_U()
      Opcodes.I64_EQZ -> Compare.I64_EQZ()
      Opcodes.I64_EQ -> Compare.I64_EQ()
      Opcodes.I64_NE -> Compare.I64_NE()
      Opcodes.I64_LT_S -> Compare.I64_LT_S()
      Opcodes.I64_LT_U -> Compare.I64_LT_U()
      Opcodes.I64_GT_S -> Compare.I64_GT_S()
      Opcodes.I64_GT_U -> Compare.I64_GT_U()
      Opcodes.I64_LE_S -> Compare.I64_LE_S()
      Opcodes.I64_LE_U -> Compare.I64_LE_U()
      Opcodes.I64_GE_S -> Compare.I64_GE_S()
      Opcodes.I64_GE_U -> Compare.I64_GE_U()
      Opcodes.F32_EQ -> Compare.F32_EQ()
      Opcodes.F32_NE -> Compare.F32_NE()
      Opcodes.F32_LT -> Compare.F32_LT()
      Opcodes.F32_GT -> Compare.F32_GT()
      Opcodes.F32_LE -> Compare.F32_LE()
      Opcodes.F32_GE -> Compare.F32_GE()
      Opcodes.F64_EQ -> Compare.F64_EQ()
      Opcodes.F64_NE -> Compare.F64_NE()
      Opcodes.F64_LT -> Compare.F64_LT()
      Opcodes.F64_GT -> Compare.F64_GT()
      Opcodes.F64_LE -> Compare.F64_LE()
      Opcodes.F64_GE -> Compare.F64_GE()
      Opcodes.REF_EQ -> Compare.REF_EQ()
      else -> throw IllegalArgumentException()
    }.add()
  }

  override fun convert(opcode: UByte) {
    when (opcode) {
      Opcodes.I32_WRAP_I64 -> Convert.I32_WRAP_I64()
      Opcodes.I32_TRUNC_S_F32 -> Convert.I32_TRUNC_S_F32()
      Opcodes.I32_TRUNC_U_F32 -> Convert.I32_TRUNC_U_F32()
      Opcodes.I32_TRUNC_S_F64 -> Convert.I32_TRUNC_S_F64()
      Opcodes.I32_TRUNC_U_F64 -> Convert.I32_TRUNC_U_F64()
      Opcodes.I64_EXTEND_S_I32 -> Convert.I64_EXTEND_S_I32()
      Opcodes.I64_EXTEND_U_I32 -> Convert.I64_EXTEND_U_I32()
      Opcodes.I64_TRUNC_S_F32 -> Convert.I64_TRUNC_S_F32()
      Opcodes.I64_TRUNC_U_F32 -> Convert.I64_TRUNC_U_F32()
      Opcodes.I64_TRUNC_S_F64 -> Convert.I64_TRUNC_S_F64()
      Opcodes.I64_TRUNC_U_F64 -> Convert.I64_TRUNC_U_F64()
      Opcodes.F32_CONVERT_S_I32 -> Convert.F32_CONVERT_S_I32()
      Opcodes.F32_CONVERT_U_I32 -> Convert.F32_CONVERT_U_I32()
      Opcodes.F32_CONVERT_S_I64 -> Convert.F32_CONVERT_S_I64()
      Opcodes.F32_CONVERT_U_I64 -> Convert.F32_CONVERT_U_I64()
      Opcodes.F32_DEMOTE_F64 -> Convert.F32_DEMOTE_F64()
      Opcodes.F64_CONVERT_S_I32 -> Convert.F64_CONVERT_S_I32()
      Opcodes.F64_CONVERT_U_I32 -> Convert.F64_CONVERT_U_I32()
      Opcodes.F64_CONVERT_S_I64 -> Convert.F64_CONVERT_S_I64()
      Opcodes.F64_CONVERT_U_I64 -> Convert.F64_CONVERT_U_I64()
      Opcodes.F64_PROMOTE_F32 -> Convert.F64_PROMOTE_F32()
      else -> throw IllegalArgumentException()
    }.add()
  }

  override fun structOp(gcOpcode: UByte, type: TypeId, field: FieldId) {
    when (gcOpcode) {
      Opcodes.GC_STRUCT_SET -> StructOp.GC_STRUCT_SET(type = type, field = field)
      Opcodes.GC_STRUCT_GET -> StructOp.GC_STRUCT_GET(type = type, field = field)
      Opcodes.GC_STRUCT_GET_S -> StructOp.GC_STRUCT_GET_S(type = type, field = field)
      Opcodes.GC_STRUCT_GET_U -> StructOp.GC_STRUCT_GET_U(type = type, field = field)
      else -> throw IllegalArgumentException()
    }.add()
  }

  override fun refNull(): ValueVisitor.HeapVisitor {
    val e = Ref.NULL()
    e.add()
    return e.heap
  }

  override fun call(function: FunctionId) {
    CallFunction(function).add()
  }

  override fun callIndirect(type: TypeId, table: TableId) {
    CallIndirect(type = type, table = table).add()
  }

  override fun call(typeRef: TypeId) {
    CallType(typeRef).add()
  }

  override fun indexArgument(opcode: UByte, value: LocalId) {
    when (opcode) {
      Opcodes.GET_LOCAL -> LocalIndexArgument.GET(value)
      Opcodes.SET_LOCAL -> LocalIndexArgument.SET(value)
      Opcodes.TEE_LOCAL -> LocalIndexArgument.TEE(value)

      else -> throw IllegalArgumentException()
    }.add()
  }

  override fun indexArgument(opcode: UByte, label: GlobalId) {
    when (opcode) {
      Opcodes.GET_GLOBAL -> GlobalIndexArgument.GET(label)
      Opcodes.SET_GLOBAL -> GlobalIndexArgument.SET(label)

      else -> throw IllegalArgumentException()
    }.add()
  }

  override fun reinterpret(opcode: UByte) {
    when (opcode) {
      Opcodes.I32_REINTERPRET_F32 -> Reinterpret.I32ToF32()
      Opcodes.I64_REINTERPRET_F64 -> Reinterpret.I64ToF64()
      Opcodes.F32_REINTERPRET_I32 -> Reinterpret.F32ToI32()
      Opcodes.F64_REINTERPRET_I64 -> Reinterpret.F64ToI64()
      else -> throw IllegalArgumentException()
    }.add()
  }

  override fun arrayOp(gcOpcode: UByte, type: TypeId) {
    when (gcOpcode) {
      Opcodes.GC_ARRAY_GET -> ArrayOp.Get(type)
      Opcodes.GC_ARRAY_SET -> ArrayOp.Set(type)
      Opcodes.GC_ARRAY_GET_S -> ArrayOp.GetS(type)
      Opcodes.GC_ARRAY_GET_U -> ArrayOp.GetU(type)
      else -> throw IllegalArgumentException()
    }.add()
  }

  override fun ref(gcOpcode: UByte): ValueVisitor.HeapVisitor {
    val e = when (gcOpcode) {
      Opcodes.GC_REF_CAST -> Ref.GC_REF_CAST()
      Opcodes.GC_REF_TEST_NULL -> Ref.GC_REF_TEST_NULL()
      Opcodes.GC_REF_TEST -> Ref.GC_REF_TEST()
      Opcodes.GC_REF_CAST_NULL -> Ref.GC_REF_CAST_NULL()
      else -> throw IllegalArgumentException()
    }
    e.add()
    return e.heap
  }

  override fun structNew(type: TypeId) {
    StructNew(type)
  }

  override fun arrayCopy(from: TypeId, to: TypeId) {
    ArrayCopy(from = from, to = to).add()
  }

  override fun newArray(type: TypeId, size: UInt) {
    NewArray.Size(id = type, size = size).add()
  }

  override fun newArray(type: TypeId, data: DataId) {
    NewArray.Data(id = type, data = data).add()
  }

  override fun brOnCastFail(flags: UByte, label: LabelId): ExpressionsVisitor.BrOnCastFailVisitor {
    val e = BrOnCastFail(flags = flags, labelId = label)
    e.add()
    return e
  }

  override fun arrayFull(type: TypeId) {
    ArrayFull(type).add()
  }

  override fun newArrayDefault(type: TypeId) {
    NewArrayDefault(type).add()
  }

  override fun arrayLen() {
    ArrayLen().add()
  }

  override fun ref(function: FunctionId) {
    RefFunction(function).add()
  }

  override fun drop() {
    Drop().add()
  }

  override fun brTable(): ExpressionsVisitor.BrTableVisitor {
    val e = BrTable()
    e.add()
    return e
  }

  override fun select() {
    Select().add()
  }

  override fun selectWithType(): ExpressionsVisitor.SelectVisitor {
    val e = SelectWithType()
    e.add()
    return e
  }

  override fun throwOp(tag: TagId) {
    ThrowTag(tag).add()
  }

  override fun startBlock(opcode: UByte): ExpressionsVisitor.BlockStartVisitor {
    val e = when (opcode) {
      Opcodes.LOOP -> BlockStart.LOOP()
      Opcodes.BLOCK -> BlockStart.BLOCK()
      Opcodes.TRY -> BlockStart.TRY()
      Opcodes.IF -> BlockStart.IF()
      else -> throw IllegalArgumentException()
    }
    e.add()
    return e
  }

  override fun refIsNull() {
    RefIsNull().add()
  }

  override fun refAsNonNull() {
    RefAsNonNull().add()
  }

  override fun throwRef() {
    ThrowRef().add()
  }

  override fun catchAll() {
    CatchAll().add()
  }

  override fun memorySize(id: MemoryId) {
    MemoryOp.Size(id).add()
  }

  override fun memoryGrow(id: MemoryId) {
    MemoryOp.Grow(id).add()
  }

  override fun rethrow(v32u: UInt) {
    Rethrow(v32u).add()
  }

  override fun catch(v32u: UInt) {
    Catch(v32u).add()
  }

  override fun convertNumeric(numOpcode: UByte) {
    when (numOpcode) {
      Opcodes.NUMERIC_I32S_CONVERT_SAT_F32 -> Convert.NUMERIC_I32S_CONVERT_SAT_F32()
      Opcodes.NUMERIC_I32U_CONVERT_SAT_F32 -> Convert.NUMERIC_I32U_CONVERT_SAT_F32()
      Opcodes.NUMERIC_I32S_CONVERT_SAT_F64 -> Convert.NUMERIC_I32S_CONVERT_SAT_F64()
      Opcodes.NUMERIC_I32U_CONVERT_SAT_F64 -> Convert.NUMERIC_I32U_CONVERT_SAT_F64()
      Opcodes.NUMERIC_I64S_CONVERT_SAT_F32 -> Convert.NUMERIC_I64S_CONVERT_SAT_F32()
      Opcodes.NUMERIC_I64U_CONVERT_SAT_F32 -> Convert.NUMERIC_I64U_CONVERT_SAT_F32()
      Opcodes.NUMERIC_I64S_CONVERT_SAT_F64 -> Convert.NUMERIC_I64S_CONVERT_SAT_F64()
      Opcodes.NUMERIC_I64U_CONVERT_SAT_F64 -> Convert.NUMERIC_I64U_CONVERT_SAT_F64()

      else -> throw IllegalArgumentException()
    }.add()
  }

  override fun gcConvert(gcOpcode: UByte) {
    when (gcOpcode) {
      Opcodes.GC_ANY_CONVERT_EXTERN -> Convert.GC_ANY_CONVERT_EXTERN()
      Opcodes.GC_EXTERN_CONVERT_ANY -> Convert.GC_EXTERN_CONVERT_ANY()

      else -> throw IllegalArgumentException()
    }.add()
  }

  override fun bulkOperator(numberOpcode: UByte) {
    when (numberOpcode) {
      Opcodes.NUMERIC_MEMORY_INIT -> BulkOperator.MemoryInit()
      Opcodes.NUMERIC_DATA_DROP -> BulkOperator.DataDrop()
      Opcodes.NUMERIC_MEMORY_COPY -> BulkOperator.MemoryCopy()
      Opcodes.NUMERIC_MEMORY_FILL -> BulkOperator.MemoryFill()
      Opcodes.NUMERIC_TABLE_INIT -> BulkOperator.TableInit()
      Opcodes.NUMERIC_ELEM_DROP -> BulkOperator.ElemDrop()
      Opcodes.NUMERIC_TABLE_COPY -> BulkOperator.TableCopy()
      Opcodes.NUMERIC_TABLE_SIZE -> BulkOperator.TableSize()
      else -> throw IllegalArgumentException()
    }.add()
  }

  fun accept(visitor: ExpressionsVisitor) {
    visitor.start()
    forEach {
      visitor.beforeOperation()
      it.accept(visitor)
      visitor.afterOperation()
    }
    visitor.end()
  }
}
