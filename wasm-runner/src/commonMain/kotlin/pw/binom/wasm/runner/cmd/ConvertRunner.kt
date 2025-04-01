package pw.binom.wasm.runner.cmd

import pw.binom.wasm.node.inst.Convert
import pw.binom.wasm.node.inst.Inst
import pw.binom.wasm.runner.MemorySpace
import pw.binom.wasm.runner.stack.Stack

object ConvertRunner {
  fun run(cmd: Convert, stack: Stack): Inst? {
    return when (cmd) {
      is Convert.I32_WRAP_I64 -> {
        stack.pushI32(stack.popI64().toInt())
        cmd.next
      }

      is Convert.I64_EXTEND_U_I32 -> {
        stack.pushI64(stack.popI32().toUInt().toULong().toLong())
        cmd.next
      }

      is Convert.F32_CONVERT_S_I32 -> TODO()
      is Convert.F32_CONVERT_S_I64 -> TODO()
      is Convert.F32_CONVERT_U_I32 -> TODO()
      is Convert.F32_CONVERT_U_I64 -> TODO()
      is Convert.F32_DEMOTE_F64 -> TODO()
      is Convert.F64_CONVERT_S_I32 -> TODO()
      is Convert.F64_CONVERT_S_I64 -> TODO()
      is Convert.F64_CONVERT_U_I32 -> TODO()
      is Convert.F64_CONVERT_U_I64 -> TODO()
      is Convert.F64_PROMOTE_F32 -> TODO()
      is Convert.GC_ANY_CONVERT_EXTERN -> TODO()
      is Convert.GC_EXTERN_CONVERT_ANY -> TODO()
      is Convert.I32_EXTEND16_S -> TODO()
      is Convert.I32_EXTEND8_S -> {
        val x = stack.popI32()
        stack.pushI32((x and 0xFF).toByte().toInt())
        cmd.next
      }

      is Convert.I32_TRUNC_S_F32 -> TODO()
      is Convert.I32_TRUNC_S_F64 -> TODO()
      is Convert.I32_TRUNC_U_F32 -> TODO()
      is Convert.I32_TRUNC_U_F64 -> TODO()
      is Convert.I64_EXTEND16_S -> TODO()
      is Convert.I64_EXTEND32_S -> TODO()
      is Convert.I64_EXTEND8_S -> TODO()
      is Convert.I64_EXTEND_S_I32 -> {
        stack.pushI64(stack.popI32().toLong())
        cmd.next
      }
      is Convert.I64_TRUNC_S_F32 -> TODO()
      is Convert.I64_TRUNC_S_F64 -> TODO()
      is Convert.I64_TRUNC_U_F32 -> TODO()
      is Convert.I64_TRUNC_U_F64 -> TODO()
      is Convert.NUMERIC_I32S_CONVERT_SAT_F32 -> TODO()
      is Convert.NUMERIC_I32S_CONVERT_SAT_F64 -> TODO()
      is Convert.NUMERIC_I32U_CONVERT_SAT_F32 -> TODO()
      is Convert.NUMERIC_I32U_CONVERT_SAT_F64 -> TODO()
      is Convert.NUMERIC_I64S_CONVERT_SAT_F32 -> TODO()
      is Convert.NUMERIC_I64S_CONVERT_SAT_F64 -> TODO()
      is Convert.NUMERIC_I64U_CONVERT_SAT_F32 -> TODO()
      is Convert.NUMERIC_I64U_CONVERT_SAT_F64 -> TODO()
    }
  }
}
