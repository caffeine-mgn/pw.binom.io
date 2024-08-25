package pw.binom.wasm.writers

import pw.binom.wasm.*
import pw.binom.wasm.visitors.ExpressionsVisitor
import pw.binom.wasm.visitors.ExpressionsVisitor.BrOnCastFailVisitor
import pw.binom.wasm.visitors.ValueVisitor

class ExpressionsWriter(private val out: WasmOutput) : ExpressionsVisitor {
  private val blockStartWriter = object : ExpressionsVisitor.BlockStartVisitor {
    private var status = 0
    override fun start() {
      check(status == 0)
      status++
    }

    override fun end() {
      check(status == 2)
      status = 0
    }

    override fun valueType(): ValueVisitor {
      check(status == 1)
      status++
      return ValueWriter(out)
    }

    override fun withoutType() {
      check(status == 1)
      status++
      out.i8u(0x40u)
    }
  }

  private val brTableWriter = object : ExpressionsVisitor.BrTableVisitor {
    private val NONE = 0
    private val STARTED = 0
    private val TARGETS = 0
    private val DEFAULT = 1

    private var state = 0
    private val targets = ArrayList<Int>()
    private var default = 0

    override fun start() {
      check(state == NONE)
    }

    override fun end() {
      check(state == STARTED || state == DEFAULT)
      out.v32u(targets.size.toUInt())
      targets.forEach {
        out.v32u(it.toUInt())
      }
      out.v32u(default.toUInt())
      state = NONE
    }

    override fun target(label: LabelId) {
      check(state == STARTED || state == TARGETS)
      state = TARGETS
      targets += label.id.toInt()
    }

    override fun default(label: LabelId) {
      check(state == STARTED || state == TARGETS)
      state = DEFAULT
      default = label.id.toInt()
    }
  }

  private val brOnCastFailWriter = object : BrOnCastFailVisitor {
    private var state = 0
    override fun start() {
      check(state == 0)
      state++
    }

    override fun end() {
      check(state == 3)
      state = 0
    }

    override fun source(): ValueVisitor.HeapVisitor {
      check(state == 1)
      state++
      return ValueWriter(out)
    }

    override fun target(): ValueVisitor.HeapVisitor {
      check(state == 2)
      state++
      return ValueWriter(out)
    }
  }

  override fun startBlock(opcode: UByte): ExpressionsVisitor.BlockStartVisitor {
    when (opcode) {
      Opcodes.LOOP,
      Opcodes.BLOCK,
      Opcodes.TRY,
      Opcodes.IF,
        -> {
        out.i8u(opcode)
        return blockStartWriter
      }

      else -> throw IllegalArgumentException()
    }
  }

  override fun endBlock() {
    out.i8u(Opcodes.END)
  }

  override fun select() {
    out.i8u(Opcodes.SELECT)
  }

  override fun indexArgument(opcode: UByte, label: GlobalId) {
    when (opcode) {
      Opcodes.GET_GLOBAL,
      Opcodes.SET_GLOBAL,
        -> {
        out.i8u(opcode)
        out.v32u(label.id)
      }

      else -> throw IllegalArgumentException()
    }
  }

  override fun indexArgument(opcode: UByte, label: LocalId) {
    when (opcode) {
      Opcodes.GET_LOCAL,
      Opcodes.SET_LOCAL,
      Opcodes.TEE_LOCAL,
        -> {
        out.i8u(opcode)
        out.v32u(label.id)
      }

      else -> throw IllegalArgumentException()
    }
  }

  override fun br(opcode: UByte, label: LabelId) {
    when (opcode) {
      Opcodes.BR,
      Opcodes.BR_IF,
      Opcodes.BR_ON_NULL,
      Opcodes.BR_ON_NON_NULL,
        -> {
        out.i8u(opcode)
        out.v32u(label.id)
      }

      else -> throw IllegalArgumentException()
    }
  }

  override fun brTable(): ExpressionsVisitor.BrTableVisitor {
    out.i8u(Opcodes.BR_TABLE)
    return brTableWriter
  }

  override fun controlFlow(opcode: UByte) {
    when (opcode) {
      Opcodes.UNREACHABLE,
      Opcodes.NOP,
      Opcodes.ELSE,
      Opcodes.RETURN,
        -> out.i8u(opcode)

      else -> throw IllegalArgumentException()
    }
  }

  override fun drop() {
    out.i8u(Opcodes.DROP)
  }

  override fun memory(opcode: UByte, align: UInt, offset: UInt) {
    when (opcode) {
      Opcodes.I32_LOAD,
      Opcodes.I64_LOAD,
      Opcodes.F32_LOAD,
      Opcodes.F64_LOAD,
      Opcodes.I32_LOAD8_S,
      Opcodes.I32_LOAD8_U,
      Opcodes.I32_LOAD16_S,
      Opcodes.I32_LOAD16_U,
      Opcodes.I64_LOAD8_S,
      Opcodes.I64_LOAD8_U,
      Opcodes.I64_LOAD16_S,
      Opcodes.I64_LOAD16_U,
      Opcodes.I64_LOAD32_S,
      Opcodes.I64_LOAD32_U,
      Opcodes.I32_STORE,
      Opcodes.I64_STORE,
      Opcodes.F32_STORE,
      Opcodes.F64_STORE,
      Opcodes.I32_STORE8,
      Opcodes.I32_STORE16,
      Opcodes.I64_STORE8,
      Opcodes.I64_STORE16,
      Opcodes.I64_STORE32,
        -> {
        out.i8u(opcode)
        out.v32u(align)
        out.v32u(offset)
      }

      else -> throw IllegalArgumentException()
    }
  }

  override fun numeric(opcode: UByte) {
    when (opcode) {
      Opcodes.I32_CLZ,
      Opcodes.I32_CTZ,
      Opcodes.I32_POPCNT,
      Opcodes.I32_ADD,
      Opcodes.I32_SUB,
      Opcodes.I32_MUL,
      Opcodes.I32_DIV_S,
      Opcodes.I32_DIV_U,
      Opcodes.I32_REM_S,
      Opcodes.I32_REM_U,
      Opcodes.I32_AND,
      Opcodes.I32_OR,
      Opcodes.I32_XOR,
      Opcodes.I32_SHL,
      Opcodes.I32_SHR_S,
      Opcodes.I32_SHR_U,
      Opcodes.I32_ROTL,
      Opcodes.I32_ROTR,
      Opcodes.I64_CLZ,
      Opcodes.I64_CTZ,
      Opcodes.I64_POPCNT,
      Opcodes.I64_ADD,
      Opcodes.I64_SUB,
      Opcodes.I64_MUL,
      Opcodes.I64_DIV_S,
      Opcodes.I64_DIV_U,
      Opcodes.I64_REM_S,
      Opcodes.I64_REM_U,
      Opcodes.I64_AND,
      Opcodes.I64_OR,
      Opcodes.I64_XOR,
      Opcodes.I64_SHL,
      Opcodes.I64_SHR_S,
      Opcodes.I64_SHR_U,
      Opcodes.I64_ROTL,
      Opcodes.I64_ROTR,
      Opcodes.F32_ABS,
      Opcodes.F32_NEG,
      Opcodes.F32_CEIL,
      Opcodes.F32_FLOOR,
      Opcodes.F32_TRUNC,
      Opcodes.F32_NEAREST,
      Opcodes.F32_SQRT,
      Opcodes.F32_ADD,
      Opcodes.F32_SUB,
      Opcodes.F32_MUL,
      Opcodes.F32_DIV,
      Opcodes.F32_MIN,
      Opcodes.F32_MAX,
      Opcodes.F32_COPYSIGN,
      Opcodes.F64_ABS,
      Opcodes.F64_NEG,
      Opcodes.F64_CEIL,
      Opcodes.F64_FLOOR,
      Opcodes.F64_TRUNC,
      Opcodes.F64_NEAREST,
      Opcodes.F64_SQRT,
      Opcodes.F64_ADD,
      Opcodes.F64_SUB,
      Opcodes.F64_MUL,
      Opcodes.F64_DIV,
      Opcodes.F64_MIN,
      Opcodes.F64_MAX,
      Opcodes.F64_COPYSIGN,
        -> {
        out.i8u(opcode)
      }

      else -> throw IllegalArgumentException()
    }
  }

  override fun const(value: Float) {
    out.i32s(value.toBits())
  }

  override fun const(value: Double) {
    out.i64s(value.toBits())
  }

  override fun const(value: Int) {
    out.i32s(value)
  }

  override fun const(value: Long) {
    out.i64s(value)
  }

  override fun compare(opcode: UByte) {
    when (opcode) {
      Opcodes.I32_EQZ,
      Opcodes.I32_EQ,
      Opcodes.I32_NE,
      Opcodes.I32_LT_S,
      Opcodes.I32_LT_U,
      Opcodes.I32_GT_S,
      Opcodes.I32_GT_U,
      Opcodes.I32_LE_S,
      Opcodes.I32_LE_U,
      Opcodes.I32_GE_S,
      Opcodes.I32_GE_U,
      Opcodes.I64_EQZ,
      Opcodes.I64_EQ,
      Opcodes.I64_NE,
      Opcodes.I64_LT_S,
      Opcodes.I64_LT_U,
      Opcodes.I64_GT_S,
      Opcodes.I64_GT_U,
      Opcodes.I64_LE_S,
      Opcodes.I64_LE_U,
      Opcodes.I64_GE_S,
      Opcodes.I64_GE_U,
      Opcodes.F32_EQ,
      Opcodes.F32_NE,
      Opcodes.F32_LT,
      Opcodes.F32_GT,
      Opcodes.F32_LE,
      Opcodes.F32_GE,
      Opcodes.F64_EQ,
      Opcodes.F64_NE,
      Opcodes.F64_LT,
      Opcodes.F64_GT,
      Opcodes.F64_LE,
      Opcodes.F64_GE,
      Opcodes.REF_EQ,
        -> out.i8u(opcode)

      else -> throw IllegalArgumentException()
    }
  }

  override fun convert(opcode: UByte) {
    when (opcode) {
      Opcodes.I32_WRAP_I64,
      Opcodes.I32_TRUNC_S_F32,
      Opcodes.I32_TRUNC_U_F32,
      Opcodes.I32_TRUNC_S_F64,
      Opcodes.I32_TRUNC_U_F64,
      Opcodes.I64_EXTEND_S_I32,
      Opcodes.I64_EXTEND_U_I32,
      Opcodes.I64_TRUNC_S_F32,
      Opcodes.I64_TRUNC_U_F32,
      Opcodes.I64_TRUNC_S_F64,
      Opcodes.I64_TRUNC_U_F64,
      Opcodes.F32_CONVERT_S_I32,
      Opcodes.F32_CONVERT_U_I32,
      Opcodes.F32_CONVERT_S_I64,
      Opcodes.F32_CONVERT_U_I64,
      Opcodes.F32_DEMOTE_F64,
      Opcodes.F64_CONVERT_S_I32,
      Opcodes.F64_CONVERT_U_I32,
      Opcodes.F64_CONVERT_S_I64,
      Opcodes.F64_CONVERT_U_I64,
      Opcodes.F64_PROMOTE_F32,
        -> out.i8u(opcode)

      else -> throw IllegalArgumentException()
    }
  }

  override fun ref(function: FunctionId) {
    out.i8u(Opcodes.REF_FUNC)
    out.v32u(function.id)
  }

  override fun refIsNull() {
    out.i8u(Opcodes.REF_IS_NULL)
  }

  override fun refAsNonNull() {
    out.i8u(Opcodes.REF_AS_NON_NULL)
  }

  override fun call(opcode: UByte, type: TypeId, table: TableId) {
    out.i8u(Opcodes.CALL_INDIRECT)
    out.v32u(type.value)
    out.v32u(table.id)
  }

  override fun call(opcode: UByte, function: FunctionId) {
    out.i8u(Opcodes.CALL)
    out.v32u(function.id)
  }

  override fun call(opcode: UByte, typeRef: TypeId) {
    out.i8u(Opcodes.CALL_REF)
    out.v32u(typeRef.value)
  }

  override fun throwOp(tag: TagId) {
    out.i8u(Opcodes.THROW)
    out.v32u(tag.value)
  }

  override fun throwRef() {
    out.i8u(Opcodes.THROW_REF)
  }

  override fun catchAll() {
    out.i8u(Opcodes.CATCH_ALL)
  }

  override fun reinterpret(opcode: UByte) {
    when (opcode) {
      Opcodes.I32_REINTERPRET_F32,
      Opcodes.I64_REINTERPRET_F64,
      Opcodes.F32_REINTERPRET_I32,
      Opcodes.F64_REINTERPRET_I64,
        -> out.i8u(opcode)

      else -> throw IllegalArgumentException()
    }
  }

  override fun structNew(type: TypeId) {
    out.i8u(Opcodes.GC_PREFIX)
    out.i8u(Opcodes.GC_STRUCT_NEW)
    out.v32u(type.value)
  }

  override fun structOp(gcOpcode: UByte, type: TypeId, field: FieldId) {
    when (gcOpcode) {
      Opcodes.GC_STRUCT_SET,
      Opcodes.GC_STRUCT_GET,
      Opcodes.GC_STRUCT_GET_S,
      Opcodes.GC_STRUCT_GET_U,
        -> {
        out.i8u(Opcodes.GC_PREFIX)
        out.i8u(gcOpcode)
        out.v32u(type.value)
        out.v32u(field.id)
      }

      else -> throw IllegalArgumentException()
    }
  }

  override fun newArrayDefault(type: TypeId) {
    out.i8u(Opcodes.GC_PREFIX)
    out.i8u(Opcodes.GC_ARRAY_NEW_DEFAULT)
    out.v32u(type.value)
  }

  override fun arrayOp(gcOpcode: UByte, type: TypeId) {
    when (gcOpcode) {
      Opcodes.GC_ARRAY_GET,
      Opcodes.GC_ARRAY_SET,
      Opcodes.GC_ARRAY_GET_S,
      Opcodes.GC_ARRAY_GET_U,
        -> {
        out.i8u(Opcodes.GC_PREFIX)
        out.i8u(gcOpcode)
        out.v32u(type.value)
      }
    }
  }

  override fun arrayLen() {
    out.i8u(Opcodes.GC_PREFIX)
    out.i8u(Opcodes.GC_ARRAY_LEN)
  }

  override fun arrayFull(type: TypeId) {
    out.i8u(Opcodes.GC_PREFIX)
    out.i8u(Opcodes.GC_ARRAY_FILL)
    out.v32u(type.value)
  }

  override fun arrayCopy(from: TypeId, to: TypeId) {
    out.i8u(Opcodes.GC_PREFIX)
    out.i8u(Opcodes.GC_ARRAY_COPY)
    out.v32u(from.value)
    out.v32u(to.value)
  }

  override fun newArray(type: TypeId, data: DataId) {
    out.i8u(Opcodes.GC_PREFIX)
    out.i8u(Opcodes.GC_ARRAY_NEW_DATA)
    out.v32u(type.value)
    out.v32u(data.id)
  }

  override fun newArray(type: TypeId, size: UInt) {
    out.i8u(Opcodes.GC_PREFIX)
    out.i8u(Opcodes.GC_ARRAY_NEW_FIXED)
    out.v32u(type.value)
    out.v32u(size)
  }

  override fun brOnCastFail(flags: UByte, label: LabelId): ExpressionsVisitor.BrOnCastFailVisitor {
    out.i8u(Opcodes.GC_PREFIX)
    out.i8u(Opcodes.GC_BR_ON_CAST_FAIL)
    out.i8u(flags)
    out.v32u(label.id)
    return brOnCastFailWriter
  }

  override fun ref(gcOpcode: UByte): ValueVisitor.HeapVisitor {
    when (gcOpcode) {
      Opcodes.GC_REF_CAST,
      Opcodes.GC_REF_TEST_NULL,
      Opcodes.GC_REF_TEST,
      Opcodes.GC_REF_CAST_NULL,
        -> {
        out.i8u(Opcodes.GC_PREFIX)
        out.i8u(gcOpcode)
        return ValueWriter(out)
      }

      else -> throw IllegalArgumentException()
    }
  }

  override fun refNull(): ValueVisitor.HeapVisitor {
    out.i8u(Opcodes.REF_NULL)
    return ValueWriter(out)
  }

  override fun memorySize(size: UInt) {
    out.i8u(Opcodes.MEMORY_SIZE)
    out.v32u(size)
  }

  override fun memoryGrow(size: UInt) {
    out.i8u(Opcodes.MEMORY_GROW)
    out.v32u(size)
  }

  override fun rethrow(v32u: UInt) {
    out.i8u(Opcodes.RETHROW)
    out.v32u(v32u)
  }

  override fun catch(v32u: UInt) {
    out.i8u(Opcodes.CATCH)
    out.v32u(v32u)
  }

  override fun select(typeCount: Int): ValueVisitor {
    out.i8u(Opcodes.SELECT_WITH_TYPE)
    out.v32s(typeCount)
    return ValueWriter(out)
  }

  override fun convertNumeric(opcode: UByte) {
    when (opcode) {
      Opcodes.NUMERIC_I32S_CONVERT_SAT_F32,
      Opcodes.NUMERIC_I32U_CONVERT_SAT_F32,
      Opcodes.NUMERIC_I32S_CONVERT_SAT_F64,
      Opcodes.NUMERIC_I32U_CONVERT_SAT_F64,
      Opcodes.NUMERIC_I64S_CONVERT_SAT_F32,
      Opcodes.NUMERIC_I64U_CONVERT_SAT_F32,
      Opcodes.NUMERIC_I64S_CONVERT_SAT_F64,
      Opcodes.NUMERIC_I64U_CONVERT_SAT_F64,
        -> {
        out.i8u(Opcodes.NUMERIC_PREFIX)
        out.i8u(opcode)
      }

      else -> throw IllegalArgumentException()
    }
  }

  override fun gcConvert(opcode: UByte) {
    when (opcode) {
      Opcodes.GC_ANY_CONVERT_EXTERN,
      Opcodes.GC_EXTERN_CONVERT_ANY,
        -> {
        out.i8u(Opcodes.GC_PREFIX)
        out.i8u(opcode)
      }

      else -> throw IllegalArgumentException()
    }
  }
}
