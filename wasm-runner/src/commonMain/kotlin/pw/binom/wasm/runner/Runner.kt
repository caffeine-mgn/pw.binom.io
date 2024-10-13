package pw.binom.wasm.runner

import pw.binom.collections.LinkedList
import pw.binom.wasm.AbsHeapType
import pw.binom.wasm.FunctionId
import pw.binom.wasm.Primitive
import pw.binom.wasm.node.*
import pw.binom.wasm.node.inst.*

class Runner(private val module: WasmModule, importResolver: ImportResolver) {
  private val importFunc = module.importSection.filterIsInstance<Import.Function>()

  private val importFuncImpl = importFunc.map {
    importResolver.func(
      module = it.module,
      field = it.field
    ) ?: TODO()
  }

  private val tables: List<Table> = module.tableSection.map {
    if (it.type.refNullAbs == AbsHeapType.TYPE_REF_ABS_HEAP_FUNC_REF) {
      Table.FuncTable(size = (it.max ?: it.min).toInt())
    } else {
      TODO()
    }
  }

  val memory = module.memorySection
    .map { MemorySpaceByteBuffer(1024 * 1024) } + module.importSection.asSequence()
    .filterIsInstance<Import.Memory>()
    .map { value ->
      importResolver.memory(
        module = value.module,
        field = value.field,
        inital = value.initial,
        max = (value as? Import.Memory2)?.maximum
      ) ?: TODO()
    }
    .toList()

  init {
//    check(module.elementSection.size == tables.size)
    module.elementSection.forEachIndexed { index, element ->
      when (element) {
        is Element.Type0 -> {
          val table = tables[index] as Table.FuncTable
          val offset = runCmd(
            cmds = element.expressions.first!!,
            locals = ArrayList(),
            args = ArrayList(),
            resultSize = 1,
            functionId = FunctionId(0u),
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
          cmds = expressions.first!!,
          locals = ArrayList(),
          args = ArrayList(),
          resultSize = 1,
          functionId = FunctionId(0u),
        ).single() as Int
      } ?: 0
      val memIndex = (data.memoryId?.raw ?: 0u).toInt()
      val mem = memory[memIndex]
      data.data.forEachIndexed { index, byte ->
        mem.pushI8(value = byte, offset = (index + offset).toUInt(), align = 1u)
      }
    }
  }

  private val global3 = module.globalSection.map { v ->
    when {
      v.type.number != null -> {
        when (v.type.number!!.type) {
          Primitive.I32 -> {
//            check(v.expressions.size == 2)
//            check(v.expressions.last() is EndBlock)
            val ex = v.expressions.first
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
      ) ?: TODO()
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
    val functionIndex = id.id.toInt() - importFunc.size
    val typeIndex = module.functionSection[functionIndex]
    val desc = module.typeSection[typeIndex].single!!.single!!.type as RecType.FuncType
    check(desc.args.size == args.size) { "desc.args.size=${desc.args.size}, args.size=${args.size}" }
    val code = module.codeSection[functionIndex]
    val locals = ArrayList<Variable>()
    code.locals.forEach {
      repeat(it.count.toInt()) { _ ->
        locals += Variable.create(it.type)
      }
    }
    return runCmd(
      cmds = code.code.first!!,
      locals = locals,
      args = args.toMutableList(),
      resultSize = desc.results.size,
      functionId = id,
    )
  }

  class Block1(val startIndex: Inst, val endIndex: Inst, val loop: Boolean)

  private fun findEndBlock(startIndex: Inst): Inst {
    var depth = 1
    var cmd: Inst? = startIndex
    while (cmd != null) {
      if (cmd is BlockStart) {
        depth++
        cmd = cmd.next
        continue
      }
      if (cmd is EndBlock) {
        depth--
        if (depth == 0) {
          return cmd
        }
        cmd = cmd.next
        continue
      }
      cmd = cmd.next
    }
    TODO()
  }

  private var stopped = false
  private fun callFunction(
    functionId: FunctionId,
    stack: LinkedStack,
  ) {
    if (functionId.id.toInt() in importFunc.indices) {
      val externalFun = importFunc[functionId.id.toInt()]
      val functionType = module.typeSection[externalFun.index].single!!.single!!.type as RecType.FuncType
      val l = LinkedList<Variable>()
      functionType.args.forEach {
        val v = Variable.create(it)
        v.popFromStack(stack)
        l.addFirst(v)
      }
      val impl = importFuncImpl[functionId.id.toInt()]
      impl(object : ExecuteContext {
        override val runner: Runner
          get() = this@Runner
        override val args: List<Variable>
          get() = l

        override fun stop() {
          stopped = true
        }

        override fun pushResult(value: Variable) {
          value.pushToStack(stack)
        }
      })
      return
    }
    val funcForCall = (functionId.id.toInt() - importFunc.size)
    val desc =
      module.typeSection[module.functionSection[funcForCall]].single!!.single!!.type as RecType.FuncType
    val l = LinkedList<Any>()
    desc.args.forEach { _ ->
      l.addFirst(stack.pop())
    }
//    val l = desc.args.map { stack.pop() }
    runFunc(functionId, args = l).forEach {
      stack.push(it)
    }
  }

  private fun runCmd(
    functionId: FunctionId,
    cmds: Inst,
    locals: MutableList<Variable>,
    args: MutableList<Any>,
    resultSize: Int,
  ): List<Any> {
    val funcName = module.exportSection.filterIsInstance<Export.Function>().find {
      it.id == functionId
    }?.name
    if (funcName != null) {
//      println("Call function \"$funcName\"")
    }
//    val badFunction = funcName == "__fwritex"
//    if (funcName == "__fwritex") {
//      println("call bad function __fwritex")
//    }
    val stack = LinkedStack()
    val blocks = LinkedList<Block1>()
    try {
      var cmd: Inst? = cmds
      while (!stopped && cmd != null) {
        when (cmd) {
          is BlockStart.BLOCK -> {
            val endIndex = findEndBlock(startIndex = cmd.next!!)
            blocks += Block1(startIndex = cmd, endIndex = endIndex, loop = false)
            cmd = cmd.next
          }

          is BlockStart.LOOP -> {
            val endIndex = findEndBlock(startIndex = cmd.next!!)
            blocks += Block1(startIndex = cmd, endIndex = endIndex, loop = true)
            cmd = cmd.next
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
                cmd = block.startIndex
              } else {
                cmd = block.endIndex.next
              }
            } else {
              cmd = cmd.next
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
              cmd = block.startIndex
            } else {
              cmd = block.endIndex.next
            }
          }

          is BrTable -> {
            val branch = stack.popI32()
            val label = cmd.targets.getOrNull(branch) ?: cmd.default ?: TODO()

            var block: Block1 = blocks.removeLast()
            var d = label.id
            while (d > 0u) {
              d--
              block = blocks.removeLast()
            }
            cmd = block.endIndex.next
          }

          is I32Const -> {
            stack.pushI32(cmd.value)
            cmd = cmd.next
          }

          is I64Const -> {
            stack.pushI64(cmd.value)
            cmd = cmd.next
          }

          is LocalIndexArgument.SET -> {
            try {
              val e = cmd.id.id.toInt()
              if (e in args.indices) {
                args[e] = stack.pop()
              } else {
                locals[e - args.size].popFromStack(stack)
              }
              cmd = cmd.next
            } catch (e: Throwable) {
              throw e
            }
          }

          is LocalIndexArgument.TEE -> {
            val value = stack.peek()
            val e = cmd.id.id.toInt()
            if (e in args.indices) {
              args[e] = value
            } else {
              locals[e - args.size].peekToStack(stack)
            }
            cmd = cmd.next
          }

          is LocalIndexArgument.GET -> {
            val e = cmd.id.id.toInt()
            if (e in args.indices) {
              val value = args[e]
              when (value) {
                is Int -> stack.pushI32(value)
                is Long -> stack.pushI64(value)
                else -> TODO()
              }
            } else {
              locals[e - args.size].pushToStack(stack)
            }

//            stack.push(value)
            cmd = cmd.next
          }

          is ControlFlow.RETURN -> {
            check(stack.size == resultSize)
            return stack.clear()
          }

          is ControlFlow.UNREACHABLE -> {
            TODO("UNREACHABLE")
          }

          is CallFunction -> {
            callFunction(cmd.id, stack)
            cmd = cmd.next
          }

          is CallIndirect -> {
            val tableIndex = stack.popI32()
            val table = tables[cmd.table.id.toInt()] as Table.FuncTable
            val functionId = table[tableIndex]
              ?: TODO("Function with index $tableIndex not defined!")
            callFunction(functionId, stack)
            cmd = cmd.next
          }

          is GlobalIndexArgument.GET -> {
            if (cmd.id.id.toInt() in globals2.indices) {
              globals2[cmd.id.id.toInt()].putInto(stack)
            } else {
              global3[cmd.id.id.toInt() - globals2.size].putInto(stack)
            }
            cmd = cmd.next
          }

          is GlobalIndexArgument.SET -> {
            if (cmd.id.id.toInt() in globals2.indices) {
              (globals2[cmd.id.id.toInt()] as GlobalVarMutable).setFrom(stack)
            } else {
              (global3[cmd.id.id.toInt() - globals2.size] as GlobalVarMutable).setFrom(stack)
            }
            cmd = cmd.next
          }

          is Compare.I32_EQZ -> {
            val a = stack.popI32()
            stack.pushI32(if (a == 0) 1 else 0)
            cmd = cmd.next
          }

          is Compare.I64_EQZ -> {
            val a = stack.popI64()
            stack.pushI32(if (a == 0L) 1 else 0)
            cmd = cmd.next
          }

          is Compare.I32_EQ -> {
            val a = stack.popI32()
            val b = stack.popI32()
            stack.pushI32(if (b == a) 1 else 0)
            cmd = cmd.next
          }

          is Compare.I32_NE -> {
            val a = stack.popI32()
            val b = stack.popI32()
            stack.pushI32(if (b != a) 1 else 0)
            cmd = cmd.next
          }

          is Compare.I64_NE -> {
            val a = stack.popI64()
            val b = stack.popI64()
            stack.pushI32(if (b != a) 1 else 0)
            cmd = cmd.next
          }

          is Compare.I64_GT_S -> {
            val a = stack.popI64()
            val b = stack.popI64()
            stack.pushI32(if (b > a) 1 else 0)
            cmd = cmd.next
          }

          is Compare.I32_GT_S -> {
            val a = stack.popI32()
            val b = stack.popI32()
            stack.pushI32(if (b > a) 1 else 0)
            cmd = cmd.next
          }

          is Compare.I32_GT_U -> {
            val a = stack.popI32().toUInt()
            val b = stack.popI32().toUInt()
            stack.pushI32(if (b > a) 1 else 0)
            cmd = cmd.next
          }

          is Compare.I32_GE_S -> {
            val a = stack.popI32()
            val b = stack.popI32()
            stack.pushI32(if (b >= a) 1 else 0)
            cmd = cmd.next
          }

          is Compare.I64_GE_S -> {
            val a = stack.popI64()
            val b = stack.popI64()
            stack.pushI32(if (b >= a) 1 else 0)
            cmd = cmd.next
          }

          is Convert.I32_WRAP_I64 -> {
            stack.pushI32(stack.popI64().toInt())
            cmd = cmd.next
          }

          is Convert.I64_EXTEND_U_I32 -> {
            stack.pushI64(stack.popI32().toUInt().toULong().toLong())
            cmd = cmd.next
          }

          is Compare.I64_GE_U -> {
            val a = stack.popI64().toULong()
            val b = stack.popI64().toULong()
            stack.pushI32(if (b >= a) 1 else 0)
            cmd = cmd.next
          }

          is Compare.I32_GE_U -> {
            val a = stack.popI32().toUInt()
            val b = stack.popI32().toUInt()
            stack.pushI32(if (b >= a) 1 else 0)
            cmd = cmd.next
          }

          is Compare.I32_LT_S -> {
            val a = stack.popI32()
            val b = stack.popI32()
            stack.pushI32(if (b < a) 1 else 0)
            cmd = cmd.next
          }

          is Compare.I32_LT_U -> {
            val a = stack.popI32().toUInt()
            val b = stack.popI32().toUInt()
            stack.pushI32(if (b < a) 1 else 0)
            cmd = cmd.next
          }


          is Select -> {
            val v = stack.popI32()
            val v2 = stack.pop()
            val v1 = stack.pop()
            if (v1::class != v2::class) {
              TODO()
            }
            stack.push(if (v != 0) v1 else v2)
            cmd = cmd.next
          }

          is Compare.I32_LE_S -> {
            val a = stack.popI32()
            val b = stack.popI32()
            stack.pushI32(if (b <= a) 1 else 0)
            cmd = cmd.next
          }

          is Compare.I32_LE_U -> {
            val a = stack.popI32().toUInt()
            val b = stack.popI32().toUInt()
            stack.pushI32(if (b <= a) 1 else 0)
            cmd = cmd.next
          }

          is Numeric.I32_ROTL -> {
            val a = stack.popI32()
            val distance = stack.popI32()
            val result = (a shl distance) or (a ushr -distance)
            stack.pushI32(result)
            cmd = cmd.next
          }

          is Numeric.I32_CLZ -> {
            val value = stack.popI32()
            stack.pushI32(value.countLeadingZeroBits())
            cmd = cmd.next
          }

          is Numeric.I32_CTZ -> {
            val value = stack.popI32()
            stack.pushI32(value.countTrailingZeroBits())
            cmd = cmd.next
          }

          is Numeric.I32_AND -> {
            val a = stack.popI32()
            val b = stack.popI32()
            stack.pushI32(a and b)
            cmd = cmd.next
          }

          is Numeric.I32_OR -> {
            val a = stack.popI32()
            val b = stack.popI32()
            stack.pushI32(a or b)
            cmd = cmd.next
          }

          is Numeric.I32_ADD -> {
            val a = stack.popI32()
            val b = stack.popI32()
            stack.pushI32(b + a)
            cmd = cmd.next
          }

          is Numeric.I32_SHL -> {
            val b = stack.popI32()
            val a = stack.popI32()
            stack.pushI32(a shl b)
            cmd = cmd.next
          }

          is Numeric.I32_SHR_S -> {
            val b = stack.popI32()
            val a = stack.popI32()
            stack.pushI32(a shr b)
            cmd = cmd.next
          }

          is Numeric.I32_SHR_U -> {
            val b = stack.popI32()
            val a = stack.popI32()
            stack.pushI32(a ushr b)
            cmd = cmd.next
          }

          is Numeric.I32_SUB -> {
            val a = stack.popI32()
            val b = stack.popI32()
            stack.pushI32(b - a)
            cmd = cmd.next
          }

          is Numeric.I32_MUL -> {
            val a = stack.popI32()
            val b = stack.popI32()
            stack.pushI32(b * a)
            cmd = cmd.next
          }

          is Numeric.I64_MUL -> {
            val a = stack.popI64()
            val b = stack.popI64()
            stack.pushI64(b * a)
            cmd = cmd.next
          }

          is Numeric.I32_DIV_S -> {
            val a = stack.popI32()
            val b = stack.popI32()
            stack.pushI32(b / a)
            cmd = cmd.next
          }

          is Numeric.I32_DIV_U -> {
            val a = stack.popI32().toUInt()
            val b = stack.popI32().toUInt()
            stack.pushI32((b / a).toInt())
            cmd = cmd.next
          }

          is Numeric.I32_REM_S -> {
            val a = stack.popI32()
            val b = stack.popI32()
            stack.pushI32(b % a)
            cmd = cmd.next
          }

          is Numeric.I32_REM_U -> {
            val a = stack.popI32().toUInt()
            val b = stack.popI32().toUInt()
            stack.pushI32((b % a).toInt())
            cmd = cmd.next
          }

          is Numeric.I32_XOR -> {
            val a = stack.popI32()
            val b = stack.popI32()
            stack.pushI32(b xor a)
            cmd = cmd.next
          }

          is Drop -> {
            stack.pop()
            cmd = cmd.next
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
            cmd = cmd.next
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
            cmd = cmd.next
          }

          is Memory.I32_STORE16 -> {
            val value = stack.popI32()
            val address = stack.popI32()
            val mem = memory[cmd.memoryId.raw.toInt()]
            mem.pushI16(
              value = value.toShort(),
              offset = address.toUInt() + cmd.offset,
              align = cmd.align,
            )
            cmd = cmd.next
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
            cmd = cmd.next
          }

          is Memory.I64_LOAD32_S -> {
            val offset = stack.popI32()
            val mem = memory[cmd.memoryId.raw.toInt()]
            val address = cmd.offset + offset.toUInt()
            val value = mem.getI32(address).toLong()
            stack.pushI64(value)
            cmd = cmd.next
          }

          is MemoryOp.Size -> {
            val mem = memory[cmd.id.raw.toInt()]
            stack.push(mem.limit / MemorySpaceByteArray.PAGE_SIZE)
            cmd = cmd.next
          }

          is MemoryOp.Grow -> {
            val size = stack.pop() as Int
            val mem = memory[cmd.id.raw.toInt()]
            val r = mem.grow(size.toUInt()) ?: TODO()
            stack.pushI32(r.toInt())
            cmd = cmd.next
          }

          is Memory.I64_LOAD32_U -> {
            val offset = stack.popI32()
            val mem = memory[cmd.memoryId.raw.toInt()]
            val address = cmd.offset + offset.toUInt()
            val value = mem.getI32(address).toUInt().toLong()
            stack.pushI64(value)
            cmd = cmd.next
          }

          is Memory.I32_LOAD8_U -> {
            val offset = stack.popI32()
            val mem = memory[cmd.memoryId.raw.toInt()]
            val address = cmd.offset + offset.toUInt()
            val value = mem.getI8(address).toInt()
            stack.pushI32(value)
            cmd = cmd.next
          }

          is Memory.I32_LOAD8_S -> {
            val offset = stack.popI32()
            val mem = memory[cmd.memoryId.raw.toInt()]
            val address = cmd.offset + offset.toUInt()
            val value = mem.getI8(address).toInt()
            stack.pushI32(value)
            cmd = cmd.next
          }

          is Memory.I32_LOAD16_U -> {
            val offset = stack.popI32()
            val mem = memory[cmd.memoryId.raw.toInt()]
            val address = cmd.offset + offset.toUInt()
            val value = mem.getI16(address).toInt()
            stack.pushI32(value)
            cmd = cmd.next
          }

          is Memory.I32_LOAD -> {
            val offset = stack.popI32()
            val mem = memory[cmd.memoryId.raw.toInt()]
            val address = cmd.offset + offset.toUInt()
            val value = mem.getI32(address)
            stack.pushI32(value)
            cmd = cmd.next
          }

          is Memory.I64_LOAD -> {
            val offset = stack.popI32()
            val mem = memory[cmd.memoryId.raw.toInt()]
            stack.pushI64(
              mem.getI64(
                cmd.offset + offset.toUInt()
              )
            )
            cmd = cmd.next
          }

          is EndBlock -> {
            if (cmd.next != null) {
              blocks.removeLast()
            } else {
              check(stack.size == resultSize) { "Invalid stack and result. resultSize=$resultSize, stack.size=${stack.size}" }
              return stack.clear()
            }
            cmd = cmd.next
          }

          else -> TODO("Unknown ${cmd::class} ")
        }
      }
    } catch (e: Throwable) {
      throw RuntimeException("Can't execute task", e)
    }
    return stack.clear()
  }
}
