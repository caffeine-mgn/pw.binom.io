package pw.binom.wasm.runner.cmd

import pw.binom.wasm.node.inst.Compare
import pw.binom.wasm.node.inst.Inst
import pw.binom.wasm.runner.MemorySpace
import pw.binom.wasm.runner.Value
import pw.binom.wasm.runner.stack.Stack

object CompareRunner {
  fun run(cmd: Compare, stack: Stack): Inst? {
    val eq:Boolean = when (cmd) {
      is Compare.F32_EQ -> {
        val a = stack.popF32()
        val b = stack.popF32()
        b == a
      }
      is Compare.F32_GE -> {
        val a = stack.popF32()
        val b = stack.popF32()
        b >= a
      }
      is Compare.F32_GT -> {
        val a = stack.popF32()
        val b = stack.popF32()
        b > a
      }
      is Compare.F32_LE -> {
        val a = stack.popF32()
        val b = stack.popF32()
        b <= a
      }
      is Compare.F32_LT -> {
        val a = stack.popF32()
        val b = stack.popF32()
        b < a
      }
      is Compare.F32_NE -> {
        val a = stack.popF32()
        val b = stack.popF32()
        b != a
      }
      is Compare.F64_EQ -> {
        val a = stack.popF64()
        val b = stack.popF64()
        b == a
      }
      is Compare.F64_GE -> {
        val a = stack.popF64()
        val b = stack.popF64()
        b >= a
      }
      is Compare.F64_GT -> {
        val a = stack.popF64()
        val b = stack.popF64()
        b > a
      }
      is Compare.F64_LE -> {
        val a = stack.popF64()
        val b = stack.popF64()
        b <= a
      }
      is Compare.F64_LT -> {
        val a = stack.popF64()
        val b = stack.popF64()
        b < a
      }
      is Compare.F64_NE -> {
        val a = stack.popF64()
        val b = stack.popF64()
        b != a
      }
      is Compare.I32_EQ -> {
        val a = stack.popI32()
        val b = stack.popI32()
        b == a
      }

      is Compare.I32_EQZ -> {
        val a = stack.popI32()
        a == 0
      }

      is Compare.I32_GE_S -> {
        val a = stack.popI32()
        val b = stack.popI32()
        b >= a
      }

      is Compare.I32_GE_U -> {
        val a = stack.popI32().toUInt()
        val b = stack.popI32().toUInt()
        b >= a
      }

      is Compare.I32_GT_S -> {
        val a = stack.popI32()
        val b = stack.popI32()
        b > a
      }

      is Compare.I32_GT_U -> {
        val a = stack.popI32().toUInt()
        val b = stack.popI32().toUInt()
        b > a
      }

      is Compare.I32_LE_S -> {
        val a = stack.popI32()
        val b = stack.popI32()
        b <= a
      }

      is Compare.I32_LE_U -> {
        val a = stack.popI32().toUInt()
        val b = stack.popI32().toUInt()
        b <= a
      }

      is Compare.I32_LT_S -> {
        val a = stack.popI32()
        val b = stack.popI32()
        b < a
      }

      is Compare.I32_LT_U -> {
        val a = stack.popI32().toUInt()
        val b = stack.popI32().toUInt()
        b < a
      }

      is Compare.I32_NE -> {
        val a = stack.popI32()
        val b = stack.popI32()
        b != a
      }

      is Compare.I64_EQ -> {
        val a = stack.popI64()
        val b = stack.popI64()
        b == a
      }

      is Compare.I64_EQZ -> {
        val a = stack.popI64()
        a == 0L
      }

      is Compare.I64_GE_S -> {
        val a = stack.popI64()
        val b = stack.popI64()
        b >= a
      }

      is Compare.I64_GE_U -> {
        val a = stack.popI64().toULong()
        val b = stack.popI64().toULong()
        b >= a
      }

      is Compare.I64_GT_S -> {
        val a = stack.popI64()
        val b = stack.popI64()
        b > a
      }

      is Compare.I64_GT_U -> {
        val a = stack.popI64().toULong()
        val b = stack.popI64().toULong()
        b > a
      }
      is Compare.I64_LE_S -> {
        val a = stack.popI64()
        val b = stack.popI64()
        b <= a
      }
      is Compare.I64_LE_U -> {
        val a = stack.popI64().toULong()
        val b = stack.popI64().toULong()
        b <= a
      }
      is Compare.I64_LT_S -> {
        val a = stack.popI64()
        val b = stack.popI64()
        b < a
      }
      is Compare.I64_LT_U -> {
        val a = stack.popI64().toULong()
        val b = stack.popI64().toULong()
        b < a
      }
      is Compare.I64_NE -> {
        val a = stack.popI64()
        val b = stack.popI64()
        b != a
      }

      is Compare.REF_EQ -> TODO()
    }
    stack.pushI32(if (eq) 1 else 0)
    return cmd.next
  }
}
