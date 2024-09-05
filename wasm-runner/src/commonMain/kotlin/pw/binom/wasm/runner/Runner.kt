package pw.binom.wasm.runner

import pw.binom.collections.LinkedList
import pw.binom.wasm.FunctionId
import pw.binom.wasm.node.Export
import pw.binom.wasm.node.RecType
import pw.binom.wasm.node.WasmModule
import pw.binom.wasm.node.inst.*

class Runner(private val module: WasmModule) {
  private val memorySpace = MemorySpace()

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

    fun local(index: Int) =
      args.getOrNull(index) ?: locals.getOrNull(index - args.size)

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
          if (cmd.id.id.toInt() in args.indices) {
            args[cmd.id.id.toInt()] = stack.pop()
          } else {
            locals[index - args.size].set(stack.pop())
          }
          index++
        }

        is LocalIndexArgument.TEE -> {
          val value = stack.pop()
          stack.push(value)
          val e =cmd.id.id.toInt()
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
          val value = if (cmd.id.id.toInt() in module.importSection.elements.indices){
            module.importSection.elements[cmd.id.id.toInt()]
          } else {
            module.globalSection.elements[index - module.importSection.elements.size]
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

        is Memory.I32_STORE -> {
          TODO()
          memorySpace.pushInt(
            value = stack.popI32(),
            offset = cmd.offset,
            align = cmd.align,
          )
          index++
        }

        else -> TODO("Unknown ${cmd::class}")
      }
    }
  }
}
