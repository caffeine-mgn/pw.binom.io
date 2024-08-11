package pw.binom.wasm

import pw.binom.io.EOFException
import pw.binom.io.use

/**
 * https://webassembly.github.io/exception-handling/core/binary/modules.html#binary-codesec
 * https://webassembly.github.io/exception-handling/core/binary/instructions.html#binary-instr
 * https://github.com/WebAssembly/design/blob/main/BinaryEncoding.md#function-bodies
 * https://chromium.googlesource.com/v8/v8/+/refs/heads/main/src/wasm/wasm-opcodes.h
 * https://www.w3.org/TR/wasm-core-2/
 */
object CodeSectionReader {
  private class Context {
    var depth = 1
  }

  fun read(input: InputReader, visitor: CodeSectionVisitor) {
    val sizeInBytes = input.readVarUInt32L()
    visitor.start(sizeInBytes.toIntExact())
    if (sizeInBytes.toIntExact() == 0) {
      visitor.end()
      return
    }
    input.withLimit(sizeInBytes.toIntExact()).use { sectionInput ->
      sectionInput.readVec {
        val size = sectionInput.readVarUInt32L()
        val type = sectionInput.readValueType()
        repeat(size.toIntExact()) {
          visitor.local(type)
        }
      }
      readExpressions(input = sectionInput, visitor = ExpressionsVisitor.STUB)
    }
  }

  fun readExpressions(input: InputReader, visitor: ExpressionsVisitor) {
    val context = Context()
    var lastOpcode = Opcodes.END
    while (true) {
      if (context.depth == 0 && lastOpcode == Opcodes.END) {
//        input.skipOther()
        break
      }
      val opcode = try {
        input.readUByte()
      } catch (e: EOFException) {
        break
      }
      lastOpcode = opcode
      when (opcode) {
        Opcodes.GC_PREFIX -> gc(visitor = ExpressionsVisitor.STUB, input = input)
        Opcodes.SIMD_PREFIX -> smid(input = input, visitor = ExpressionsVisitor.STUB)
        Opcodes.NUMERIC_PREFIX -> numeric(input = input, visitor = ExpressionsVisitor.STUB)
        else -> default(input = input, visitor = ExpressionsVisitor.STUB, opcode = opcode, context = context)
      }
    }
//    input.skipOther()
    visitor.end()
    check(context.depth == 0)
    check(lastOpcode == Opcodes.END) {
      "Function should end with Opcodes.END, last opcode: 0x${
        lastOpcode.toString(
          16
        )
      }"
    }
  }

  private fun smid(input: InputReader, visitor: ExpressionsVisitor) {
    val opcode = input.readUByte()
    when (opcode) {
      else -> TODO("Unknown SMID code: 0x${opcode.toString(16)}")
    }
  }

  private fun default(opcode: UByte, context: Context, input: InputReader, visitor: ExpressionsVisitor) {
    println("OPCODE      (0x${opcode.toString(16).padStart(2, '0')}) ${Codes.codes[opcode]}")
    when (opcode) {
      Opcodes.THROW_REF -> {
        // TODO
      }

      Opcodes.GET_LOCAL,
      Opcodes.SET_LOCAL,
      Opcodes.TEE_LOCAL,
      Opcodes.GET_GLOBAL,
      Opcodes.SET_GLOBAL,
        -> {
        input.readVarUInt32AsInt()
//        visitor.indexArgument(opcode, input.readVarUInt32AsInt())
      }

      Opcodes.SELECT -> {
        // TODO
      }

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
        input.readVarUInt32AsInt()
        input.readVarUInt32()
//        visitor.memOpAlignOffsetArg(opcode, input.readVarUInt32AsInt(), input.readVarUInt32())
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
        -> {
//        visitor.numOp(opcode)
      }

      Opcodes.LOOP,
      Opcodes.BLOCK,
      Opcodes.TRY,
      Opcodes.IF,
        -> {
        context.depth++
        input.readBlockType()
        // TODO
      }

      Opcodes.END -> {
        context.depth--
//        visitor.controlFlow(opcode)
      }

      Opcodes.DROP -> {
        // TODO
      }

      Opcodes.F32_CONST -> {
        input.readInt32()
//        visitor.const(opcode, Float.fromBits(input.readInt32()))
      }

      Opcodes.I64_CONST -> {
        val value = input.readLebSigned()
//        input.readSignedLeb128(Long.SIZE_BYTES)
      }

      Opcodes.F64_CONST -> {
        val value = input.readUInt64()
//        input.readSignedLeb128(Long.SIZE_BYTES)
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
        -> {
//        visitor.compare(opcode)
      }

      Opcodes.BR,
      Opcodes.BR_IF,
        -> {
        input.readVarUInt32AsInt()
//        visitor.controlFlow(opcode, input.readVarUInt32AsInt())
      }

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
        -> {
//        visitor.convert(opcode)
      }

      Opcodes.REF_EQ -> {

      }

      Opcodes.UNREACHABLE,
      Opcodes.NOP,
      Opcodes.ELSE,
      Opcodes.RETURN,
        -> {
//        visitor.controlFlow(opcode)
      }

      Opcodes.I32_CONST -> {
        input.readVarInt32()
//        visitor.const(opcode, input.readVarInt32())
      }

      Opcodes.REF_FUNC -> {
        input.readVarInt32()
        // TODO
      }

      Opcodes.REF_IS_NULL -> {
        // TODO
      }

      Opcodes.REF_AS_NON_NULL -> {
        // TODO
      }

      Opcodes.REF_NULL -> {
        input.readHeapType()
        // TODO
      }

      Opcodes.CALL_INDIRECT -> {
        input.readVarUInt32AsInt()
        input.readVarUInt32AsInt()
        // TODO
      }

      Opcodes.CALL -> {
        input.readVarUInt32AsInt()
        // TODO
      }

      Opcodes.CALL_REF -> {
        input.readVarUInt32AsInt()
      }

      Opcodes.MEMORY_SIZE -> {
        input.readVarUInt32()
      }

      Opcodes.MEMORY_GROW -> {
        input.readVarUInt32()
      }

      Opcodes.THROW -> {
        input.readVarUInt32L()
        // TODO
      }

      Opcodes.CATCH -> {
        input.readVarUInt32L()
//        input.readLebUnsigned()
      }

      Opcodes.BR_TABLE -> {
        // targets
        input.readVec { input.readVarInt32() }

        // default target
        input.readVarInt32()
      }

      Opcodes.SELECT_WITH_TYPE -> {
        val typeCount = input.readVarInt32()
        check(typeCount == 1)
        input.readValueType()
      }

      Opcodes.RETHROW -> {
        input.readVarInt32()
      }

      Opcodes.CATCH_ALL -> {
      }

      Opcodes.I32_REINTERPRET_F32,
      Opcodes.I64_REINTERPRET_F64,
      Opcodes.F32_REINTERPRET_I32,
      Opcodes.F64_REINTERPRET_I64,
        -> {

      }

      else -> TODO("Unknown opcode: 0x${opcode.toString(16)}")
    }

  }

  private fun numeric(input: InputReader, visitor: ExpressionsVisitor) {
    val opcode = input.readUByte()
    val opcodeStr = "0x" +
      Opcodes.NUMERIC_PREFIX.toString(16).padStart(2, '0') +
      opcode.toString(16).padStart(2, '0')
    println("OPCODE NUMERIC 0xFC $opcode ($opcodeStr) ${Codes.numericCodes[opcode]}")
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
      }

      else -> TODO("Unknown MISC code: 0x${opcode.toString(16)}")
    }
  }

  private fun gc(input: InputReader, visitor: ExpressionsVisitor) {
    val opcode = input.readUByte()
    println("OPCODE GC $opcode (0x${opcode.toString(16)}) ${Codes.gcCodes[opcode]}")
    when (opcode) {
      Opcodes.GC_STRUCT_NEW -> {
        // ref.null type_id
        input.readVarUInt32() // type_id
      }

      Opcodes.GC_REF_CAST_NULL -> {
        // ref.cast heapType
        input.readHeapType() // heapType
      }

      Opcodes.GC_STRUCT_GET -> {
        input.readVarUInt32() // typeid
        input.readVarUInt32() // fieldid
      }

      Opcodes.GC_STRUCT_SET -> {
        input.readVarUInt32() // typeid
        input.readVarUInt32() // fieldid
      }

      Opcodes.GC_REF_TEST -> {
        input.readHeapType()
      }

      Opcodes.GC_REF_TEST_NULL -> {
        input.readHeapType()
      }

      Opcodes.GC_REF_CAST -> {
        input.readHeapType()
      }

      Opcodes.GC_ARRAY_NEW_DEFAULT -> {
        input.readVarUInt32()
      }

      Opcodes.GC_ARRAY_GET -> {
        input.readVarUInt32()
      }

      Opcodes.GC_ARRAY_SET -> {
        input.readVarUInt32() // typeid
      }

      Opcodes.GC_ARRAY_GET_S -> {
        input.readVarUInt32() // typeid
      }

      Opcodes.GC_ARRAY_LEN -> {

      }

      Opcodes.GC_ARRAY_GET_U -> {
        input.readVarUInt32() // typeid
      }

      Opcodes.GC_ARRAY_COPY -> {
        input.readVarUInt32() // typeid
        input.readVarUInt32() // typeid
      }

      Opcodes.GC_ARRAY_NEW_DATA -> {
        input.readVarUInt32() // typeid
        input.readVarUInt32() // dataid
      }

      Opcodes.GC_ARRAY_NEW_FIXED -> {
        input.readVarUInt32() // typeid
        input.readVarUInt32()
      }

      Opcodes.GC_STRUCT_GET_S -> {
        input.readVarUInt32() // typeid
        input.readVarUInt32() // field
      }

      Opcodes.GC_STRUCT_GET_U -> {
        input.readVarUInt32() // typeid
        input.readVarUInt32() // field
      }

      Opcodes.GC_BR_ON_CAST_FAIL -> {
        input.readUByte() // flags
        input.readVarUInt32() // branch
        input.readHeapType() // source_imm
        input.readHeapType() // target_imm
      }

      Opcodes.GC_EXTERN_CONVERT_ANY -> {

      }

      Opcodes.GC_ANY_CONVERT_EXTERN -> {

      }

      else -> TODO(
        "Unknown GC code: 0x${Opcodes.GC_PREFIX.toString(16).padStart(2, '0')}${
          opcode.toString(16).padStart(2, '0')
        }"
      )
    }
  }
}
