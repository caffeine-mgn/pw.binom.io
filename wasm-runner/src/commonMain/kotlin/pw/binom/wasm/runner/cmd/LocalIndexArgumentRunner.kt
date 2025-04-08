package pw.binom.wasm.runner.cmd

import pw.binom.wasm.node.inst.Inst
import pw.binom.wasm.node.inst.LocalIndexArgument
import pw.binom.wasm.runner.MutableValue2
import pw.binom.wasm.runner.stack.Stack

object LocalIndexArgumentRunner {
  fun run(
    cmd: LocalIndexArgument,
    args: List<MutableValue2>,
    locals: List<MutableValue2>,
    stack: Stack,
  ): Inst? {
    return when (cmd) {
      is LocalIndexArgument.GET -> {
        val e = cmd.id.id.toInt()
        if (e in args.indices) {
          args[e].pushToStack(stack)
        } else {
          locals[e - args.size].pushToStack(stack)
        }

//            stack.push(value)
        cmd.next
      }

      is LocalIndexArgument.SET -> {
        val e = cmd.id.id.toInt()
        if (e in args.indices) {
          args[e].popFromStack(stack)
        } else {
          locals[e - args.size].popFromStack(stack)
        }
        cmd.next
      }

      is LocalIndexArgument.TEE -> {
        val e = cmd.id.id.toInt()
        if (e in args.indices) {
          args[e].peekToStack(stack)
        } else {
          locals[e - args.size].peekToStack(stack)
        }
        cmd.next
      }
    }
  }
}
