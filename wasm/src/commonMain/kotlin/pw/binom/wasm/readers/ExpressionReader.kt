package pw.binom.wasm.readers

import pw.binom.io.EOFException
import pw.binom.wasm.*
import pw.binom.wasm.visitors.ExpressionsVisitor

/**
 * https://webassembly.github.io/exception-handling/core/binary/instructions.html#binary-instr
 * https://webassembly.github.io/gc/core/binary/instructions.html#binary-instr
 */
object ExpressionReader {
  val POW_2_6 = 2u * 2u * 2u * 2u * 2u * 2u
  val POW_2_7 = POW_2_6 * 2u
  private class Context {
    var depth = 1
  }

  fun readExpressions(input: WasmInput, visitor: ExpressionsVisitor) {
    val context = Context()
    var lastOpcode = Opcodes.END
    input as StreamReader
    while (true) {
      if (context.depth == 0 && lastOpcode == Opcodes.END) {
        break
      }
      val before = input.globalCursor
      val opcode = try {
        input.i8u()
      } catch (e: EOFException) {
        break
      }
      lastOpcode = opcode
//      println("OPCODE: 0x${opcode.toString(16).padStart(2, '0')}")


      visitor.beforeOperation()
      val opcodeStart = input.globalCursor
      when (opcode) {
        Opcodes.GC_PREFIX -> gc(visitor = visitor, input = input)
        Opcodes.SIMD_PREFIX -> smid(input = input, visitor = visitor)
        Opcodes.NUMERIC_PREFIX -> numeric(input = input, visitor = visitor)
        else -> default(input = input, visitor = visitor, opcode = opcode, context = context)
      }
      visitor.afterOperation()
      val size = input.globalCursor - before
      if (readFunctionCount == BAD_FUNCTION_BLOCK || BAD_FUNCTION_BLOCK == ALL) {

        if (readOpCount == BAD_OP || BAD_OP == ALL) {
          println(
            "READ #$readOpCount SIZE: $size, opcode: 0x${
              opcode.toString(16).padStart(2, '0')
            } on 0x${opcodeStart.toString(16)}\n"
          )
        }
      }
      if (lastWriteOpSize != size) {
//        TODO("Invalid operation size! $readOpCount, opcode: 0x${opcode.toString(16)}, should be $size, but got $lastWriteOpSize")
      }
      if (readFunctionCount == BAD_FUNCTION_BLOCK || BAD_FUNCTION_BLOCK == ALL) {

      }
      readOpCount++
    }
//    input.skipOther()
    visitor.end()
    check(context.depth == 0)
  }

  private fun smid(input: WasmInput, visitor: ExpressionsVisitor) {
    val opcode = input.i8u()
    when (opcode) {
      else -> TODO("Unknown SMID code: 0x${opcode.toString(16)}")
    }
  }

  private fun default(opcode: UByte, context: Context, input: WasmInput, visitor: ExpressionsVisitor) {
//    println("OPCODE      (0x${opcode.toString(16).padStart(2, '0')}) ${Codes.codes[opcode]}")
    when (opcode) {
      Opcodes.GET_LOCAL,
      Opcodes.SET_LOCAL,
      Opcodes.TEE_LOCAL,
        -> visitor.indexArgument(opcode = opcode, value = LocalId(input.v32u()))

      Opcodes.GET_GLOBAL,
      Opcodes.SET_GLOBAL,
        -> visitor.indexArgument(opcode = opcode, value = GlobalId(input.v32u()))

      Opcodes.SELECT -> visitor.select()

      Opcodes.LOOP,
      Opcodes.BLOCK,
      Opcodes.TRY,
      Opcodes.IF,
        -> {
        context.depth++
        val v = visitor.startBlock(opcode)
        v.start()
        input.readBlockType(v)
        v.end()
      }

      Opcodes.END -> {
        visitor.endBlock()
        context.depth--
//        visitor.controlFlow(opcode)
      }

      Opcodes.BR,
      Opcodes.BR_IF,
      Opcodes.BR_ON_NULL,
      Opcodes.BR_ON_NON_NULL,
        -> visitor.br(
        opcode = opcode,
        label = LabelId(input.v32u()),
      )

      Opcodes.BR_TABLE -> {
        val visitor = visitor.brTable()
        visitor.start()
        input.readVec {
          visitor.target(LabelId(input.v32u()))
        }
        visitor.default(LabelId(input.v32u()))
        visitor.end()
      }

      Opcodes.UNREACHABLE,
      Opcodes.NOP,
      Opcodes.ELSE,
      Opcodes.RETURN,
        -> visitor.controlFlow(opcode)

      Opcodes.DROP -> visitor.drop()

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
        var align = input.v32u()
        val memoryId: MemoryId
        when {
          POW_2_6 < align && align < POW_2_7 -> {
            align -= POW_2_6
            memoryId = MemoryId(input.v32u())
          }

          align < POW_2_6 -> memoryId = MemoryId(0u)
          else -> throw IllegalStateException("Illegal align value: $align")
        }
        val offset = input.v32u()

        visitor.memory(
          opcode = opcode,
          align = align,
          offset = offset,
          memoryId = memoryId,
        )
      }

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
        -> visitor.numeric(opcode)

      Opcodes.F32_CONST -> {
        val value = Float.fromBits(input.i32s())
        visitor.const(value)
      }

      Opcodes.I64_CONST -> {
        if (readOpCount == BAD_OP || BAD_OP == ALL) {
          println()
        }
        val value = input.v64s()
        if (readOpCount == BAD_OP || BAD_OP == ALL) {
//          println("reading i64s $value")
        }
        visitor.const(value)
      }

      Opcodes.F64_CONST -> {
        val value = Double.fromBits(input.i64s())
        visitor.const(value)
      }

      Opcodes.I32_CONST -> {
        input as StreamReader
        val start = input.globalCursor
        if (readOpCount == BAD_OP) {
          println("")
        }
        val value = input.v32s()
        if (readOpCount == BAD_OP) {
          println("reading i32 $value")
          println("INT from $start to ${input.globalCursor - start}")
        }
        visitor.const(value)
      }

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
        -> visitor.compare(opcode)

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
        -> visitor.convert(opcode)

      Opcodes.REF_FUNC -> visitor.ref(FunctionId(input.v32u()))

      Opcodes.REF_IS_NULL -> visitor.refIsNull()

      Opcodes.REF_AS_NON_NULL -> visitor.refAsNonNull()

      Opcodes.REF_NULL -> input.readHeapType(visitor = visitor.refNull())

      Opcodes.CALL_INDIRECT -> visitor.callIndirect(
        type = TypeId(input.v32u()),
        table = TableId(input.v32u()),
      )

      Opcodes.CALL -> visitor.call(
        function = FunctionId(input.v32u()),
      )

      Opcodes.CALL_REF -> visitor.call(
        typeRef = TypeId(input.v32u()),
      )

      Opcodes.MEMORY_SIZE -> visitor.memorySize(id = MemoryId(input.v32u()))

      Opcodes.MEMORY_GROW -> visitor.memoryGrow(id = MemoryId(input.v32u()))

      Opcodes.THROW -> visitor.throwOp(tag = TagId(input.v32u()))

      Opcodes.THROW_REF -> visitor.throwRef()

      Opcodes.RETHROW -> visitor.rethrow(input.v32u())

      Opcodes.CATCH -> visitor.catch(input.v32u())

      Opcodes.CATCH_ALL -> visitor.catchAll()

      Opcodes.SELECT_WITH_TYPE -> {
        val typeCount = input.v32s()
        val v = visitor.selectWithType()
        v.start()
        repeat(typeCount) {
          input.readValueType(visitor = v.type())
        }
        v.end()
      }

      Opcodes.I32_REINTERPRET_F32,
      Opcodes.I64_REINTERPRET_F64,
      Opcodes.F32_REINTERPRET_I32,
      Opcodes.F64_REINTERPRET_I64,
        -> visitor.reinterpret(opcode = opcode)

      else -> TODO("Unknown opcode: 0x${opcode.toString(16)}")
    }

  }

  private fun numeric(input: WasmInput, visitor: ExpressionsVisitor) {
    val opcode = input.i8u()
    val opcodeStr = "0x" +
      Opcodes.NUMERIC_PREFIX.toString(16).padStart(2, '0') +
      opcode.toString(16).padStart(2, '0')
//    println("OPCODE NUMERIC 0xFC $opcode ($opcodeStr) ${Codes.numericCodes[opcode]}")
    when (opcode) {
      Opcodes.NUMERIC_I32S_CONVERT_SAT_F32,
      Opcodes.NUMERIC_I32U_CONVERT_SAT_F32,
      Opcodes.NUMERIC_I32S_CONVERT_SAT_F64,
      Opcodes.NUMERIC_I32U_CONVERT_SAT_F64,
      Opcodes.NUMERIC_I64S_CONVERT_SAT_F32,
      Opcodes.NUMERIC_I64U_CONVERT_SAT_F32,
      Opcodes.NUMERIC_I64S_CONVERT_SAT_F64,
      Opcodes.NUMERIC_I64U_CONVERT_SAT_F64,
        -> visitor.convertNumeric(opcode)

      Opcodes.NUMERIC_MEMORY_INIT,
      Opcodes.NUMERIC_DATA_DROP,
      Opcodes.NUMERIC_MEMORY_COPY,
      Opcodes.NUMERIC_MEMORY_FILL,
      Opcodes.NUMERIC_TABLE_INIT,
      Opcodes.NUMERIC_ELEM_DROP,
      Opcodes.NUMERIC_TABLE_COPY,
      Opcodes.NUMERIC_TABLE_SIZE,
        -> visitor.bulkOperator(opcode)

      else -> TODO("Unknown MISC code: 0xfc${opcode.toString(16).padStart(2, '0')}")
    }
  }

  private fun gc(input: WasmInput, visitor: ExpressionsVisitor) {
    val opcode = input.i8u()
    if (readFunctionCount == BAD_FUNCTION_BLOCK || BAD_FUNCTION_BLOCK == ALL) {
      if (readOpCount == BAD_OP || writeOpCount == ALL) {
        println("GC OPCODE 0x${opcode.toUByte().toString(16)}")
      }
    }
//    println("OPCODE GC $opcode (0x${opcode.toString(16)}) ${Codes.gcCodes[opcode]}")
    when (opcode) {
      Opcodes.GC_STRUCT_NEW -> visitor.structNew(type = TypeId(input.v32u()))

      Opcodes.GC_STRUCT_SET,
      Opcodes.GC_STRUCT_GET,
      Opcodes.GC_STRUCT_GET_S,
      Opcodes.GC_STRUCT_GET_U,
        -> visitor.structOp(
        gcOpcode = opcode,
        type = TypeId(input.v32u()),
        field = FieldId(input.v32u())
      )

      Opcodes.GC_REF_CAST,
      Opcodes.GC_REF_TEST_NULL,
      Opcodes.GC_REF_TEST,
      Opcodes.GC_REF_CAST_NULL,
        -> input.readHeapType(visitor = visitor.ref(gcOpcode = opcode)) // heapType

      Opcodes.GC_ARRAY_NEW_DEFAULT -> visitor.newArrayDefault(type = TypeId(input.v32u()))

      Opcodes.GC_ARRAY_GET,
      Opcodes.GC_ARRAY_SET,
      Opcodes.GC_ARRAY_GET_S,
      Opcodes.GC_ARRAY_GET_U,
        -> visitor.arrayOp(
        gcOpcode = opcode,
        type = TypeId(input.v32u())
      )

      Opcodes.GC_ARRAY_LEN -> visitor.arrayLen()
      Opcodes.GC_ARRAY_FILL -> visitor.arrayFull(type = TypeId(input.v32u()))
      Opcodes.GC_ARRAY_COPY -> visitor.arrayCopy(
        from = TypeId(input.v32u()),
        to = TypeId(input.v32u()),
      )

      Opcodes.GC_ARRAY_NEW_DATA -> visitor.newArray(
        type = TypeId(input.v32u()),
        data = DataId(input.v32u()),
      )

      Opcodes.GC_ARRAY_NEW_FIXED -> {
        visitor.newArray(
          type = TypeId(input.v32u()),
          size = input.v32u(),
        )
      }

      Opcodes.GC_BR_ON_CAST_FAIL -> {
        val visitor = visitor.brOnCastFail(
          flags = input.i8u(),
          label = LabelId(input.v32u())
        )
        visitor.start()
        input.readHeapType(visitor = visitor.source()) // source_imm
        input.readHeapType(visitor = visitor.target()) // target_imm
        visitor.end()
      }

      Opcodes.GC_ANY_CONVERT_EXTERN,
      Opcodes.GC_EXTERN_CONVERT_ANY,
        -> visitor.gcConvert(opcode)


      else -> TODO(
        "Unknown GC code: 0x${Opcodes.GC_PREFIX.toString(16).padStart(2, '0')}${
          opcode.toString(16).padStart(2, '0')
        }"
      )
    }
  }
}
