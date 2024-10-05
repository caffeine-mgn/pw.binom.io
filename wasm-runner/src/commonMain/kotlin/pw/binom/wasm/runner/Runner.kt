package pw.binom.wasm.runner

import pw.binom.collections.LinkedList
import pw.binom.wasm.AbsHeapType
import pw.binom.wasm.FunctionId
import pw.binom.wasm.Primitive
import pw.binom.wasm.node.*
import pw.binom.wasm.node.inst.*

class Runner(private val module: WasmModule, importResolver: ImportResolver) {
  private val importGlobals = module.importSection.filterIsInstance<Import.Global>()
  val importFunc = module.importSection.filterIsInstance<Import.Function>()

  private val importFuncImpl = importFunc.map {
    importResolver.func(
      module = it.module,
      field = it.field
    )
  }

  private val memoryImport = module.importSection.filterIsInstance<Import.Memory>()
  val tables: List<Table> = module.tableSection.map {
    if (it.type.refNullAbs == AbsHeapType.TYPE_REF_ABS_HEAP_FUNC_REF) {
      Table.FuncTable(size = (it.max ?: it.min).toInt())
    } else {
      TODO()
    }
  }

  val memory = module.memorySection
    .map { MemorySpace(1024 * 1024) } + module.importSection.asSequence()
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

  init {
//    check(module.elementSection.size == tables.size)
    module.elementSection.forEachIndexed { index, element ->
      when (element) {
        is Element.Type0 -> {
          val table = tables[index] as Table.FuncTable
          val offset = runCmd(
            cmds = element.expressions,
            locals = ArrayList(),
            args = ArrayList(),
            resultSize = 1,
          ).single() as Int
          check(offset >= 0)
          table.offset = offset
          element.functions.forEachIndexed { functionIndex, functionId ->
            table[offset + functionIndex] = functionId
          }
        }
      }
    }

    module.dataSection.forEach { data ->
      val offset = data.expressions?.let { expressions ->
        runCmd(
          cmds = expressions,
          locals = ArrayList(),
          args = ArrayList(),
          resultSize = 1,
        ).single() as Int
      } ?: 0
      val memIndex = (data.memoryId?.raw ?: 0u).toInt()
      val mem = memory[memIndex]
      data.data.forEachIndexed { index, byte ->
        mem.data[index + offset] = byte
      }
    }
  }

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

  fun runFunc(name: String, args: List<Any>): List<Any?> {
    val func = findFunction(name = name)
      ?: TODO("Function \"$name\" not found")
    return runFunc(id = func.id, args = args)
  }

  fun runFunc(id: FunctionId, args: List<Any>): List<Any> {
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

  class Block1(val startIndex: Int, val endIndex: Int, val loop: Boolean)

  private fun findEndBlock(startIndex: Int, cmds: List<Inst>): Int {
    val it = cmds.listIterator(startIndex)
    var depth = 1
    var index = startIndex
    while (it.hasNext()) {
      val e = it.next()
      if (e is BlockStart) {
        depth++
        index++
        continue
      }
      if (e is EndBlock) {
        depth--
        if (depth == 0) {
          return index
        }
        index++
        continue
      }
      index++
    }
    TODO()
  }

  private fun runCmd(
    cmds: List<Inst>,
    locals: MutableList<LocalVar>,
    args: MutableList<Any>,
    resultSize: Int,
  ): List<Any> {
    var index = 0
    val stack = Stack()
    val blocks = LinkedList<Block1>()
    try {
      while (true) {
        val cmd = cmds[index]
        when (cmd) {
          is BlockStart.BLOCK -> {
            val endIndex = findEndBlock(startIndex = index + 1, cmds = cmds)
            blocks += Block1(startIndex = index, endIndex = endIndex, loop = false)
            index++
          }

          is BlockStart.LOOP -> {
            val endIndex = findEndBlock(startIndex = index + 1, cmds = cmds)
            blocks += Block1(startIndex = index, endIndex = endIndex, loop = true)
            index++
          }

          is Br.BR_IF -> {
            if (stack.popI32() != 0) {
              var block: Block1 = blocks.removeLast()
              var d = cmd.label.id
              while (d > 0u) {
                d--
                block = blocks.removeLast()
              }
              if (block.loop) {
                index = block.startIndex
              } else {
                index = block.endIndex + 1
              }
            } else {
              index++
            }
          }

          is Br.BR -> {
            var block: Block1 = blocks.removeLast()
            var d = cmd.label.id
            while (d > 0u) {
              d--
              block = blocks.removeLast()
            }
            if (block.loop) {
              index = block.startIndex
            } else {
              index = block.endIndex + 1
            }
          }

          is I32Const -> {
            stack.push(cmd.value)
            index++
          }

          is I64Const -> {
            stack.push(cmd.value)
            index++
          }

          is LocalIndexArgument.SET -> {
            try {
              val e = cmd.id.id.toInt()
              if (e in args.indices) {
                args[e] = stack.pop()
              } else {
                locals[e - args.size].set(stack.pop())
              }
              index++
            } catch (e: Throwable) {
              throw e
            }
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
              locals[e - args.size].get()!!
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
              val externalFun = importFunc[cmd.id.id.toInt()]
              val functionType = module.typeSection[externalFun.index].single!!.single!!.type as RecType.FuncType
              val l = LinkedList<Any>()
              repeat(functionType.args.size) {
                l.addFirst(stack.pop())
              }
              val impl = importFuncImpl[cmd.id.id.toInt()]
              impl(object : ExecuteContext {
                override val runner: Runner
                  get() = this@Runner
                override val args: List<Any>
                  get() = l

                override fun pushResult(value: Any) {
                  stack.push(value)
                }
              })
              index++
              continue
            }
            val funcForCall = (cmd.id.id.toInt() - importFunc.size)
            val desc =
              module.typeSection[module.functionSection[funcForCall]].single!!.single!!.type as RecType.FuncType
            val l = LinkedList<Any>()
            repeat(desc.args.size) {
              l.addFirst(stack.pop())
            }
            runFunc(cmd.id, args = l).forEach {
              stack.push(it)
            }
            index++
          }

          is CallIndirect -> {
            val desc = module.typeSection[cmd.type.value.toInt()].single!!.single!!.type as RecType.FuncType
            var tableIndex = stack.popI32()
            val l = LinkedList<Any>()
            repeat(desc.args.size) {
              l.addFirst(stack.pop())
            }
            val table = tables[cmd.table.id.toInt()] as Table.FuncTable
            val functionId = table[tableIndex]
              ?: TODO("Function with index $tableIndex not defined!")

            runFunc(functionId, args = l).forEach {
              stack.push(it)
            }
            index++
          }

          is GlobalIndexArgument.GET -> {
            if (cmd.id.id.toInt() in globals2.indices) {
              globals2[cmd.id.id.toInt()].putInto(stack)
            } else {
              global3[cmd.id.id.toInt() - globals2.size].putInto(stack)
            }
            index++
          }

          is GlobalIndexArgument.SET -> {
            if (cmd.id.id.toInt() in globals2.indices) {
              (globals2[cmd.id.id.toInt()] as GlobalVarMutable).setFrom(stack)
            } else {
              (global3[cmd.id.id.toInt() - globals2.size] as GlobalVarMutable).setFrom(stack)
            }
            index++
          }

          is Compare.I32_EQZ -> {
            val a = stack.popI32()
            stack.push(if (a == 0) 1 else 0)
            index++
          }

          is Compare.I32_EQ -> {
            val a = stack.popI32()
            val b = stack.popI32()
            stack.push(if (b == a) 1 else 0)
            index++
          }

          is Compare.I32_NE -> {
            val a = stack.popI32()
            val b = stack.popI32()
            stack.push(if (b != a) 1 else 0)
            index++
          }

          is Compare.I32_GT_S -> {
            val a = stack.popI32()
            val b = stack.popI32()
            stack.push(if (b > a) 1 else 0)
            index++
          }

          is Compare.I32_GT_U -> {
            val a = stack.popI32().toUInt()
            val b = stack.popI32().toUInt()
            stack.push(if (b > a) 1 else 0)
            index++
          }

          is Compare.I32_GE_S -> {
            val a = stack.popI32()
            val b = stack.popI32()
            stack.push(if (b >= a) 1 else 0)
            index++
          }

          is Compare.I32_GE_U -> {
            val a = stack.popI32().toUInt()
            val b = stack.popI32().toUInt()
            stack.push(if (b >= a) 1 else 0)
            index++
          }

          is Compare.I32_LT_S -> {
            val a = stack.popI32()
            val b = stack.popI32()
            stack.push(if (b < a) 1 else 0)
            index++
          }

          is Compare.I32_LT_U -> {
            val a = stack.popI32().toUInt()
            val b = stack.popI32().toUInt()
            stack.push(if (b < a) 1 else 0)
            index++
          }


          is Select -> {
            val v = stack.popI32()
            val v2 = stack.pop()
            val v1 = stack.pop()
            if (v1::class != v2::class) {
              TODO()
            }
            stack.push(if (v != 0) v1 else v2)
            index++
          }

          is Compare.I32_LE_S -> {
            val a = stack.popI32()
            val b = stack.popI32()
            stack.push(if (b <= a) 1 else 0)
            index++
          }

          is Numeric.I32_AND -> {
            val a = stack.popI32()
            val b = stack.popI32()
            stack.push(a and b)
            index++
          }

          is Numeric.I32_OR -> {
            val a = stack.popI32()
            val b = stack.popI32()
            stack.push(a or b)
            index++
          }

          is Numeric.I32_ADD -> {
            val a = stack.popI32()
            val b = stack.popI32()
            stack.push(b + a)
            index++
          }

          is Numeric.I32_SHL -> {
            val b = stack.popI32()
            val a = stack.popI32()
            stack.push(a shl b)
            index++
          }

          is Numeric.I32_SHR_S -> {
            val b = stack.popI32()
            val a = stack.popI32()
            stack.push(a shr b)
            index++
          }

          is Numeric.I32_SHR_U -> {
            val b = stack.popI32()
            val a = stack.popI32()
            stack.push(a ushr b)
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

          is Numeric.I32_XOR -> {
            val a = stack.popI32()
            val b = stack.popI32()
            stack.push(b xor a)
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

          is Memory.I32_STORE8 -> {
            val value = stack.popI32()
            val address = stack.popI32()
            val mem = memory[cmd.memoryId.raw.toInt()]
            mem.pushI8(
              value = value.toByte(),
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

          is Memory.I32_LOAD8_U -> {
            val offset = stack.popI32()
            val mem = memory[cmd.memoryId.raw.toInt()]
            val address = cmd.offset + offset.toUInt()
            val value = mem.getI8(address).toInt()
            stack.push(value)
            index++
          }

          is Memory.I32_LOAD -> {
            val offset = stack.popI32()
            val mem = memory[cmd.memoryId.raw.toInt()]
            val address = cmd.offset + offset.toUInt()
            val value = mem.getI32(address)
            stack.push(value)
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

          is EndBlock -> {
            if (index < cmds.lastIndex) {
              blocks.removeLast()
            } else {
              check(stack.size == resultSize)
              return stack.clear()
            }
            index++
          }

          else -> TODO("Unknown ${cmd::class} on index $index")
        }
      }
    } catch (e: Throwable) {
      throw RuntimeException("Can't execute task in index $index", e)
    }
  }
}
