package pw.binom.wasm.runner.cmd

import pw.binom.wasm.node.inst.Inst
import pw.binom.wasm.node.inst.Memory
import pw.binom.wasm.node.inst.Numeric
import pw.binom.wasm.runner.ArrayStack
import pw.binom.wasm.runner.MemorySpace

object NumericRunner {
  fun run(cmd: Numeric, stack: ArrayStack, memory:List<MemorySpace>): Inst? {
    return when (cmd){
      is Numeric.F32_ABS -> TODO()
      is Numeric.F32_ADD -> TODO()
      is Numeric.F32_CEIL -> TODO()
      is Numeric.F32_COPYSIGN -> TODO()
      is Numeric.F32_DIV -> TODO()
      is Numeric.F32_FLOOR -> TODO()
      is Numeric.F32_MAX -> TODO()
      is Numeric.F32_MIN -> TODO()
      is Numeric.F32_MUL -> TODO()
      is Numeric.F32_NEAREST -> TODO()
      is Numeric.F32_NEG -> TODO()
      is Numeric.F32_SQRT -> TODO()
      is Numeric.F32_SUB -> TODO()
      is Numeric.F32_TRUNC -> TODO()
      is Numeric.F64_ABS -> TODO()
      is Numeric.F64_ADD -> TODO()
      is Numeric.F64_CEIL -> TODO()
      is Numeric.F64_COPYSIGN -> TODO()
      is Numeric.F64_DIV -> TODO()
      is Numeric.F64_FLOOR -> TODO()
      is Numeric.F64_MAX -> TODO()
      is Numeric.F64_MIN -> TODO()
      is Numeric.F64_MUL -> TODO()
      is Numeric.F64_NEAREST -> TODO()
      is Numeric.F64_NEG -> TODO()
      is Numeric.F64_SQRT -> TODO()
      is Numeric.F64_SUB -> TODO()
      is Numeric.F64_TRUNC -> TODO()
      is Numeric.I32_ADD -> {
        val a = stack.popI32()
        val b = stack.popI32()
        stack.pushI32(b + a)
        cmd.next
      }
      is Numeric.I32_AND -> {
        val a = stack.popI32()
        val b = stack.popI32()
        stack.pushI32(a and b)
        cmd.next
      }
      is Numeric.I32_CLZ -> {
        val value = stack.popI32()
        stack.pushI32(value.countLeadingZeroBits())
        cmd.next
      }
      is Numeric.I32_CTZ -> {
        val value = stack.popI32()
        stack.pushI32(value.countTrailingZeroBits())
        cmd.next
      }
      is Numeric.I32_DIV_S -> {
        val a = stack.popI32()
        val b = stack.popI32()
        stack.pushI32(b / a)
        cmd.next
      }
      is Numeric.I32_DIV_U -> {
        val a = stack.popI32().toUInt()
        val b = stack.popI32().toUInt()
        stack.pushI32((b / a).toInt())
        cmd.next
      }
      is Numeric.I32_MUL -> {
        val a = stack.popI32()
        val b = stack.popI32()
        stack.pushI32(b * a)
        cmd.next
      }
      is Numeric.I32_OR -> {
        val a = stack.popI32()
        val b = stack.popI32()
        stack.pushI32(a or b)
        cmd.next
      }
      is Numeric.I32_POPCNT -> TODO()
      is Numeric.I32_REM_S -> {
        val a = stack.popI32()
        val b = stack.popI32()
        stack.pushI32(b % a)
        cmd.next
      }
      is Numeric.I32_REM_U -> {
        val a = stack.popI32().toUInt()
        val b = stack.popI32().toUInt()
        stack.pushI32((b % a).toInt())
        cmd.next
      }
      is Numeric.I32_ROTL -> {
        val a = stack.popI32()
        val distance = stack.popI32()
        val result = (a shl distance) or (a ushr -distance)
        stack.pushI32(result)
        cmd.next
      }
      is Numeric.I32_ROTR -> TODO()
      is Numeric.I32_SHL -> {
        val b = stack.popI32()
        val a = stack.popI32()
        stack.pushI32(a shl b)
        cmd.next
      }
      is Numeric.I32_SHR_S -> {
        val b = stack.popI32()
        val a = stack.popI32()
        stack.pushI32(a shr b)
        cmd.next
      }
      is Numeric.I32_SHR_U -> {
        val b = stack.popI32()
        val a = stack.popI32()
        stack.pushI32(a ushr b)
        cmd.next
      }
      is Numeric.I32_SUB -> {
        val a = stack.popI32()
        val b = stack.popI32()
        stack.pushI32(b - a)
        cmd.next
      }
      is Numeric.I32_XOR -> {
        val a = stack.popI32()
        val b = stack.popI32()
        stack.pushI32(b xor a)
        cmd.next
      }
      is Numeric.I64_ADD -> TODO()
      is Numeric.I64_AND -> TODO()
      is Numeric.I64_CLZ -> TODO()
      is Numeric.I64_CTZ -> TODO()
      is Numeric.I64_DIV_S -> TODO()
      is Numeric.I64_DIV_U -> TODO()
      is Numeric.I64_MUL -> {
        val a = stack.popI64()
        val b = stack.popI64()
        stack.pushI64(b * a)
        cmd.next
      }
      is Numeric.I64_OR -> TODO()
      is Numeric.I64_POPCNT -> TODO()
      is Numeric.I64_REM_S -> TODO()
      is Numeric.I64_REM_U -> TODO()
      is Numeric.I64_ROTL -> TODO()
      is Numeric.I64_ROTR -> TODO()
      is Numeric.I64_SHL -> TODO()
      is Numeric.I64_SHR_S -> TODO()
      is Numeric.I64_SHR_U -> TODO()
      is Numeric.I64_SUB -> TODO()
      is Numeric.I64_XOR -> TODO()
    }
  }
}
