package pw.binom.wasm.runner.cmd

import pw.binom.wasm.node.inst.Const
import pw.binom.wasm.node.inst.Inst
import pw.binom.wasm.runner.Value
import pw.binom.wasm.runner.stack.Stack

object ConstRunner {
  fun run(cmd: Const, stack: Stack): Inst? {
    val value = when (cmd) {
      is Const.F32Const -> Value.Primitive.F32(cmd.value)
      is Const.F64Const -> Value.Primitive.F64(cmd.value)
      is Const.I32Const -> Value.Primitive.I32(cmd.value)
      is Const.I64Const -> Value.Primitive.I64(cmd.value)
    }
    stack.push(value)
    return cmd.next
  }
}
