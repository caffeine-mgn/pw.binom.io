package pw.binom.wasm.runner

import pw.binom.collections.LinkedList
import pw.binom.wasm.FunctionId
import pw.binom.wasm.Primitive
import pw.binom.wasm.node.*
import pw.binom.wasm.node.inst.*

class Runner(private val module: WasmModule, importResolver: ImportResolver) {
  private val importGlobals = module.importSection.filterIsInstance<Import.Global>()
  val importFunc = module.importSection.filterIsInstance<Import.Function>()
  private val memoryImport = module.importSection.filterIsInstance<Import.Memory>()

  private val global3 = module.globalSection.map { v ->
    when {
      v.type.number != null -> {
        when (v.type.number!!.type) {
          Primitive.I32 -> {
            check(v.expressions.size == 2)
            check(v.expressions.last() is EndBlock)
            val ex = v.expressions.first()
            check(ex is I32Const)
            GlobalVarMutable.S32(ex.value)
          }

          else -> TODO()
        }
      }

      else -> TODO()
    }
  }

  private val globals2 = module.importSection.asSequence()
    .filterIsInstance<Import.Global>()
    .map { value ->
      importResolver.global(
        module = value.module,
        field = value.field,
        type = value.type,
        mutable = value.mutable,
      )
    }.toList()

  private val memory = module.importSection.asSequence()
    .filterIsInstance<Import.Memory>()
    .map { value ->
      importResolver.memory(
        module = value.module,
        field = value.field,
        inital = value.initial,
        max = (value as? Import.Memory2)?.maximum
      )
    }
    .toList()

//  private val functionDesc = run {
//    val result = HashMap<FunctionId, RecType.FuncType>()
//    module.functionSection.forEachIndexed { index, func ->
//      result[func] = module
//        .typeSection[func.id.toInt()]
//        .single!!
//        .single!!
//        .type as RecType.FuncType
//    }
//    result
//  }

  fun setMemory(module: String, field: String) {
    val memIndex = this.module.importSection.indexOfFirst {
      if (it !is Import.Memory) {
        return@indexOfFirst false
      }
      it.module == module && it.field == field
      true
    }
  }

  fun findFunction(name: String) =
    module.exportSection.find { it.name == name && it is Export.Function } as Export.Function?

  fun runFunc(name: String, args: List<Any?>): List<Any?> {
    val func = findFunction(name = name)
      ?: TODO("Function \"$name\" not found")
    return runFunc(id = func.id, args = args)
  }

  fun runFunc(id: FunctionId, args: List<Any?>): List<Any?> {
//    val desc = module
//      .typeSection
//      .types[module.functionSection.elements.indexOf(id)]
//      .single!!
//      .single!!
//      .type as RecType.FuncType
    val functionIndex = id.id.toInt() - importFunc.size
    val typeIndex = module.functionSection[functionIndex]
    val desc = module.typeSection[typeIndex].single!!.single!!.type as RecType.FuncType
    check(desc.args.size == args.size) { "desc.args.size=${desc.args.size}, args.size=${args.size}" }
    val code = module.codeSection[functionIndex]
    val locals = ArrayList<LocalVar>()
    code.locals.forEach {
      repeat(it.count.toInt()) {
        locals += LocalVar()
      }
    }
    return runCmd(
      cmds = code.code,
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
          if (cmd.id.id.toInt() in importFunc.indices) {
            TODO("Вызов функции из import'ов")
          }
          val funcForCall = (cmd.id.id.toInt() - importFunc.size)
          val desc = module.typeSection[module.functionSection[funcForCall]].single!!.single!!.type as RecType.FuncType
          val l = LinkedList<Any?>()
          repeat(desc.args.size) {
            l.addLast(stack.pop())
          }
          runFunc(cmd.id, args = l).forEach {
            stack.push(it)
          }
          index++
        }

        is GlobalIndexArgument.GET -> {
          if (cmd.id.id.toInt() in importGlobals.indices) {
//            val e = importGlobals[cmd.id.id.toInt()]
//            val module = globals[e.module] ?: TODO("module ${e.module} not found")
//            module[e.field] ?: TODO("Field ${e.field} not found in module ${e.module}")
            globals2[cmd.id.id.toInt()].putInto(stack)
          } else {
            global3[index - importGlobals.size].putInto(stack)
//            TODO()
//            stack.push(module.globalSection[index - importGlobals.size])
          }
          index++
        }

        is Numeric.I32_ADD -> {
          val a = stack.popI32()
          val b = stack.popI32()
          stack.push(b + a)
          index++
        }

        is Numeric.I32_SUB -> {
          val a = stack.popI32()
          val b = stack.popI32()
          stack.push(b - a)
          index++
        }

        is Numeric.I32_MUL -> {
          val a = stack.popI32()
          val b = stack.popI32()
          stack.push(b * a)
          index++
        }

        is Numeric.I32_DIV_S -> {
          val a = stack.popI32()
          val b = stack.popI32()
          stack.push(b / a)
          index++
        }

        is Numeric.I32_DIV_U -> {
          val a = stack.popI32().toUInt()
          val b = stack.popI32().toUInt()
          stack.push((b / a).toInt())
          index++
        }

        is Numeric.I32_REM_S -> {
          val a = stack.popI32()
          val b = stack.popI32()
          stack.push(b % a)
          index++
        }

        is Numeric.I32_REM_U -> {
          val a = stack.popI32().toUInt()
          val b = stack.popI32().toUInt()
          stack.push((b % a).toInt())
          index++
        }

        is Drop -> {
          stack.pop()
          index++
        }

        is Memory.I32_STORE -> {
          val value = stack.popI32()
          val address = stack.popI32()
          val mem = memory[cmd.memoryId.raw.toInt()]
          mem.pushI32(
            value = value,
            offset = address.toUInt() + cmd.offset,
            align = cmd.align,
          )
          index++
        }

        is Memory.I64_STORE -> {
          val value = stack.popI64()
          val address = stack.popI32()
          val mem = memory[cmd.memoryId.raw.toInt()]
          mem.pushI64(
            value = value,
            offset = address.toUInt() + cmd.offset,
            align = cmd.align,
          )
          index++
        }

        is Memory.I32_LOAD -> {
          val offset = stack.popI32()
          val mem = memory[cmd.memoryId.raw.toInt()]
          stack.push(
            mem.getI32(
              cmd.offset + offset.toUInt()
            )
          )
          index++
        }

        is Memory.I64_LOAD -> {
          val offset = stack.popI32()
          val mem = memory[cmd.memoryId.raw.toInt()]
          stack.push(
            mem.getI64(
              cmd.offset + offset.toUInt()
            )
          )
          index++
        }

        else -> TODO("Unknown ${cmd::class}")
      }
    }
  }
}
