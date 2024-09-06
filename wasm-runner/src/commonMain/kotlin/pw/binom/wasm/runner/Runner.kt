package pw.binom.wasm.runner

import pw.binom.collections.LinkedList
import pw.binom.wasm.FunctionId
import pw.binom.wasm.node.*
import pw.binom.wasm.node.inst.*

class Runner(private val module: WasmModule) {
  private val memorySpace = MemorySpace(100)
  private val importGlobals = module.importSection.elements.filterIsInstance<ImportGlobal>()

  private val functionDesc = run {
    val result = HashMap<FunctionId, RecType.FuncType>()
    module.functionSection.elements.forEach { func ->
      result[func] = module
        .typeSection
        .types[module.functionSection.elements.indexOf(func)]
        .single!!
        .single!!
        .type as RecType.FuncType
    }
    result
  }

  fun runFunc(name: String, args: List<Any?>): List<Any?> {
    val func = module.exportSection.elements.find { it.name == name && it is Export.Function } as Export.Function?
      ?: TODO("Function \"$name\" not found")
    return runFunc(id = func.id, args = args)
  }

  fun runFunc(id: FunctionId, args: List<Any?>): List<Any?> {
    val desc = module
      .typeSection
      .types[module.functionSection.elements.indexOf(id)]
      .single!!
      .single!!
      .type as RecType.FuncType
    check(desc.args.size == args.size) { "desc.args.size=${desc.args.size}, args.size=${args.size}" }
    val code = module.codeSection.functions[id.id.toInt()]
    val locals = ArrayList<LocalVar>()
    code.locals.forEach {
      repeat(it.count.toInt()) {
        locals += LocalVar().also { it.set(0) }
      }
    }
    return runCmd(
      cmds = code.code.elements,
      locals = locals,
      args = args.toMutableList(),
      resultSize = desc.results.size,
    )
  }

  private fun runCmd(
    cmds: List<Inst>,
    locals: MutableList<LocalVar>,
    args: MutableList<Any?>,
    resultSize: Int,
  ): List<Any?> {
    var index = 0
    val stack = Stack()
    while (true) {
      val cmd = cmds[index]
      when (cmd) {
        is I32Const -> {
          stack.push(cmd.value)
          index++
        }

        is LocalIndexArgument.SET -> {
          val e = cmd.id.id.toInt()
          if (e in args.indices) {
            args[e] = stack.pop()
          } else {
            locals[e - args.size].set(stack.pop())
          }
          index++
        }

        is LocalIndexArgument.TEE -> {
          val value = stack.pop()
          stack.push(value)
          val e = cmd.id.id.toInt()
          if (e in args.indices) {
            args[e] = value
          } else {
            locals[e - args.size].set(value)
          }
          index++
        }

        is LocalIndexArgument.GET -> {
          val e = cmd.id.id.toInt()
          val value = if (e in args.indices) {
            args[e]
          } else {
            locals[e - args.size].get()
          }
          stack.push(value)
          index++
        }

        is ControlFlow.RETURN -> {
          check(stack.size == resultSize)
          return stack.clear()
        }

        is CallFunction -> {
          val l = LinkedList<Any?>()
          repeat(functionDesc[cmd.id]!!.args.size) {
            l.addLast(stack.pop())
          }
          runFunc(cmd.id, args = l).forEach {
            stack.push(it)
          }
          index++
        }

        is GlobalIndexArgument.GET -> {
          val value = if (cmd.id.id.toInt() in importGlobals.indices) {
            val e = importGlobals[cmd.id.id.toInt()]
            val module = globals[e.module] ?: TODO("module ${e.module} not found")
            module[e.field] ?: TODO("Field ${e.field} not found in module ${e.module}")
          } else {
            module.globalSection.elements[index - importGlobals.size]
          }
          stack.push(value)
          index++
        }

        is Numeric.I32_ADD -> {
          stack.push(stack.popI32() + stack.popI32())
          index++
        }

        is Numeric.I32_SUB -> {
          stack.push(stack.popI32() - stack.popI32())
          index++
        }

        is Numeric.I32_MUL -> {
          stack.push(stack.popI32() * stack.popI32())
          index++
        }

        is Numeric.I32_DIV_S -> {
          stack.push(stack.popI32() / stack.popI32())
          index++
        }

        is Numeric.I32_DIV_U -> {
          stack.push((stack.popI32().toUInt() / stack.popI32().toUInt()).toInt())
          index++
        }
        is Numeric.I32_REM_S -> {
          stack.push(stack.popI32() % stack.popI32())
          index++
        }
        is Numeric.I32_REM_U -> {
          stack.push((stack.popI32().toUInt() % stack.popI32().toUInt()).toInt())
          index++
        }
        is Memory.I32_STORE -> {
          val value = stack.popI32()
          val address = stack.popI32()
          memorySpace.pushI32(
            value = value,
            offset = address.toUInt() + cmd.offset,
            align = cmd.align,
          )
          index++
        }

        is Memory.I64_STORE -> {
          val value = stack.popI64()
          val address = stack.popI32()
          memorySpace.pushI64(
            value = value,
            offset = address.toUInt() + cmd.offset,
            align = cmd.align,
          )
          index++
        }

        is Memory.I32_LOAD -> {
          val offset = stack.popI32()
          stack.push(
            memorySpace.getI32(
              cmd.offset + offset.toUInt()
            )
          )
          index++
        }

        is Memory.I64_LOAD -> {
          val offset = stack.popI32()
          stack.push(
            memorySpace.getI64(
              cmd.offset + offset.toUInt()
            )
          )
          index++
        }

        else -> TODO("Unknown ${cmd::class}")
      }
    }
  }

  private val globals = HashMap<String, HashMap<String, Any>>()

  fun setGlobal(module: String, field: String, value: Int) {
    globals.getOrPut(module) { HashMap() }[field] = value
  }
}
