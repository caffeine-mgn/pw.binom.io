package pw.binom.wasm.runner.cmd

import pw.binom.wasm.node.inst.Inst
import pw.binom.wasm.node.inst.Numeric
import pw.binom.wasm.runner.Value
import pw.binom.wasm.runner.stack.Stack

object NumericRunner {
  fun run(cmd: Numeric, stack: Stack): Inst? {
    return when (cmd) {
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
        val e = Value.Primitive.I32(b + a)
        stack.push(e)
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
        val shift = stack.popI32()
        val value = stack.popI32()
        stack.pushI32(value shl shift)
        cmd.next
      }

      is Numeric.I32_SHR_S -> {
        val shift = stack.popI32()
        val value = stack.popI32()
        stack.pushI32(value shr shift)
        cmd.next
      }

      is Numeric.I32_SHR_U -> {
        val shift = stack.popI32()
        val value = stack.popI32()
        stack.pushI32(value ushr shift)
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

      is Numeric.I64_ADD -> {
        val a = stack.popI64()
        val b = stack.popI64()
        stack.pushI64(b + a)
        cmd.next
      }
      is Numeric.I64_AND -> {
        val a = stack.popI64()
        val b = stack.popI64()
        stack.pushI64(a and b)
        cmd.next
      }
      is Numeric.I64_CLZ -> {
        val value = stack.popI64()
        stack.pushI64(value.countLeadingZeroBits().toLong())
        cmd.next
      }
      is Numeric.I64_CTZ -> {
        val value = stack.popI64()
        stack.pushI64(value.countTrailingZeroBits().toLong())
        cmd.next
      }
      is Numeric.I64_DIV_S -> TODO()
      is Numeric.I64_DIV_U -> TODO()
      is Numeric.I64_MUL -> {
        val a = stack.popI64()
        val b = stack.popI64()
        stack.pushI64(b * a)
        cmd.next
      }

      is Numeric.I64_OR -> {
        val a = stack.popI64()
        val b = stack.popI64()
        stack.pushI64(b or a)
        cmd.next
      }
      is Numeric.I64_POPCNT -> TODO()
      is Numeric.I64_REM_S -> TODO()
      is Numeric.I64_REM_U -> {
        val a = stack.popI64().toULong()
        val b = stack.popI64().toULong()
        stack.pushI64((b % a).toLong())
        cmd.next
      }
      is Numeric.I64_ROTL -> {
        val a = stack.popI64()
        val distance = stack.popI64()
        val result = (a shl distance.toInt()) or (a ushr -(distance.toInt()) * 2)
        stack.pushI64(result)
        cmd.next
      }
      is Numeric.I64_ROTR -> TODO()
      is Numeric.I64_SHL -> TODO()
      is Numeric.I64_SHR_S -> {
        val shift = stack.pop() as Value.Primitive.I64
        val value = stack.pop() as Value.Primitive.I64
        val result = value.value shr shift.value.toInt()
        stack.push(Value.Primitive.I64(result))
        cmd.next
      }
      is Numeric.I64_SHR_U -> TODO()
      is Numeric.I64_SUB -> {
        val a = stack.popI64()
        val b = stack.popI64()
        stack.pushI64(b - a)
        cmd.next
      }
      is Numeric.I64_XOR -> {
        val a = stack.popI64()
        val b = stack.popI64()
        stack.pushI64(b xor a)
        cmd.next
      }
    }
  }
}
