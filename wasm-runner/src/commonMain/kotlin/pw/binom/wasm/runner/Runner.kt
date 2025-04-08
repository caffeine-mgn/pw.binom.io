package pw.binom.wasm.runner

import pw.binom.collections.LinkedList
import pw.binom.wasm.AbsoluteHeapType
import pw.binom.wasm.FunctionId
import pw.binom.wasm.node.*
import pw.binom.wasm.node.inst.*
import pw.binom.wasm.node.inst.Ref
import pw.binom.wasm.runner.cmd.*
import pw.binom.wasm.runner.stack.BaseStack
import pw.binom.wasm.runner.stack.Stack

class Runner(private val module: WasmModule, importResolver: ImportResolver) {
  private val importFunc = module.importSection.filterIsInstance<Import.Function>()

  private val tables: List<Table> = module.tableSection.map {
    if (it.type.refNullAbs == AbsoluteHeapType.TYPE_REF_ABS_HEAP_FUNC_REF) {
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

  private val types = TypeDictionary.create(module.typeSection)

  private val importFuncImpl = importFunc.map {
    importResolver.func(
      module = it.module,
      field = it.field,
      type = types.getRType(it.index) as RType.Function,
    ) ?: TODO("Function ${it.module}::${it.field} not found")
  }

  init {
    module.dataSection.forEach {
      if (!it.active) {
        return@forEach
      }
      val offset = it.expressions?.let {
        runCmd(
          startCmd = it.first!!,
          locals = ArrayList(),
          args = ArrayList(),
          results = listOf(VType.Primitive(RType.Primitive.I32)),
          functionId = FunctionId(0u),
        ).single().asI32.value
      } ?: 0
      val target = memory[it.memoryId]
      target.pushBytes(src = it.data, offset = offset.toUInt())
    }

    module.elementSection.forEachIndexed { index, element ->
      when (element) {
        is Element.Type0 -> {
          val table = tables[index] as Table.FuncTable
          val offset = runCmd(
            startCmd = element.expressions.first!!,
            locals = ArrayList(),
            args = ArrayList(),
            results = listOf(VType.Primitive(RType.Primitive.I32)),
            functionId = FunctionId(0u),
          ).single().asI32.value
          check(offset >= 0)
          table.offset = offset
          element.functions.forEachIndexed { functionIndex, functionId ->
            table[offset + functionIndex] = functionId
          }
        }

        else -> TODO()
      }
    }

  }

  private val global3 = module.globalSection.map { v ->
    val vtype = TypeDictionary.convert(v.type)
    val value = runCmd(
      startCmd = v.expressions.first!!,
      locals = ArrayList(),
      args = ArrayList(),
      results = listOf(TypeDictionary.convert(v.type)),
      functionId = FunctionId(0u),
    ).single()
    if (v.mutable) {
      MutableVariable2Impl(value = value, type = vtype)
    } else {
      VariableImpl(value = value, type = vtype)
    }
    /*
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

      v.type.ref != null -> {
        val e = types[v.type.ref!!.ref!!.type!!]
        val firstInitCmd = v.expressions.first!!
        runCmd(
          startCmd = firstInitCmd,
          locals = ArrayList(),
          args = ArrayList(),
          results = listOf(TypeDictionary.convert(v.type)),
          functionId = FunctionId(0u),
        ).single().asI32.value
        TODO()
//        val instance = Instance.create(type = e, types = types)
//        val ref = GlobalVarMutable.Ref()
//        ref.ref = instance
//        ref
      }

      else -> TODO("Unknown global variable type: $v")
    }
    */
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

  fun runFunc(name: String, args: List<Variable2>): List<Any?> {
    val func = findFunction(name = name)
      ?: TODO("Function \"$name\" not found")
    return runFunc(id = func.id, args = args)
  }

  fun runFunc(id: FunctionId, args: List<Variable2>): List<Value> {
    val functionIndex = id.id.toInt() - importFunc.size
    val typeIndex = module.functionSection[functionIndex]
    val func = types.getRType(typeIndex) as RType.Function
//    val desc = module.typeSection[typeIndex].single!!.single!!.type as RecType.FuncType
    val desc = types.getRType(typeIndex) as RType.Function
    check(desc.args.size == args.size) { "desc.args.size=${desc.args.size}, args.size=${args.size}" }
    val code = module.codeSection[functionIndex]
    val locals = ArrayList<MutableValue2>()
    code.locals.forEach { localType ->
      val variableType = TypeDictionary.convert(localType.type)
      val defaultValue = variableType.default
      repeat(localType.count.toInt()) { _ ->
        locals += MutableVariable2Impl(value = defaultValue, type = variableType)
//        locals += Variable.create(it.type)
      }
    }
    return runCmd(
      startCmd = code.code.first!!,
      locals = locals,
      args = args.toMutableList(),
      results = func.results,
      functionId = id,
    )
  }

  class Block1(val startIndex: Inst, val endIndex: Inst, val loop: Boolean)

  private fun findEnd(startIndex: Inst, endIndex: Inst?, isStart: (Inst) -> Boolean, isEnd: (Inst) -> Boolean): Inst? {
    var depth = 1
    var cmd: Inst? = startIndex
    while (cmd != null) {
      if (cmd == endIndex) {
        return null
      }
      if (isStart(cmd)) {
        depth++
        cmd = cmd.next
        continue
      }
      if (isEnd(cmd)) {
        depth--
        if (depth == 0) {
          return cmd
        }
        cmd = cmd.next
        continue
      }
      cmd = cmd.next
    }
    return null
  }

  private fun findElseBlock(startIndex: Inst, endIndex: Inst) =
    findEnd(
      startIndex = startIndex,
      endIndex = endIndex,
      isStart = { it is BlockStart.IF },
      isEnd = { it is ControlFlow.ELSE }
    )

  private fun findEndBlock(startIndex: Inst): Inst =
    findEnd(
      startIndex = startIndex,
      endIndex = null,
      isStart = { it is BlockStart },
      isEnd = { it is EndBlock }
    ) ?: TODO()

  private var stopped = false
  private fun callFunction(
    functionId: FunctionId,
    stack: Stack,
  ) {
    if (functionId.id.toInt() in importFunc.indices) {
      val externalFun = importFunc[functionId.id.toInt()]
//      val functionType = module.typeSection[externalFun.index].single!!.single!!.type as RecType.FuncType
      val functionType = types[externalFun.index].single!!.type as RecType.FuncType
      val l = LinkedList<Value>()
      functionType.args.forEach {
//        val v = Variable.create(it)
//        v.popFromStack(stack)
        val type = TypeDictionary.convert(it)
        val value = stack.pop()
        l.addFirst(value)
      }
      val impl = importFuncImpl[functionId.id.toInt()]
      impl(object : ExecuteContext {
        override val runner: Runner
          get() = this@Runner
        override val args: List<Value>
          get() = l

        override fun stop() {
          stopped = true
        }

        override fun pushResult(value: Value) {
          stack.push(value)
        }
      })
      return
    }
    val funcForCall = (functionId.id.toInt() - importFunc.size)
    val funcDesc = types.getRType(module.functionSection[funcForCall]) as RType.Function
//    val desc =
//      module.typeSection[module.functionSection[funcForCall]].single!!.single!!.type as RecType.FuncType
    val l = LinkedList<Variable2>()
    funcDesc.args.forEach { type ->
      l.addFirst(VariableImpl(stack.pop(), type))
    }
    runFunc(functionId, args = l).forEach {
      stack.push(it)
    }
  }

  private fun runCmd(
    functionId: FunctionId,
    startCmd: Inst,
    locals: MutableList<MutableValue2>,
    args: MutableList<Variable2>,
    results: List<VType>,
  ): List<Value> {
    val args = args.map {
      MutableVariable2Impl(it.value, type = it.type)
    }
    val stack = BaseStack()
    val blocks = ArrayList<Block1>()
//    if (functionId.id != 0u) {
//      println("---===FUNC ${functionId.id}===---")
//    }
    try {
      var cmd: Inst? = startCmd
      while (!stopped && cmd != null) {
        when (cmd) {
          is Compare -> cmd = CompareRunner.run(cmd = cmd, stack = stack)
          is Convert -> cmd = ConvertRunner.run(cmd = cmd, stack = stack)
          is Numeric -> cmd = NumericRunner.run(cmd = cmd, stack = stack)
          is Const -> cmd = ConstRunner.run(cmd = cmd, stack = stack)
          is Memory -> cmd = MemoryRunner.run(cmd = cmd, stack = stack, memory = memory)
          is ArrayOp -> cmd =
            ArrayOpRunner.run(cmd = cmd, stack = stack, types = types, dataSection = module.dataSection)

          is BlockStart.TRY -> {
            val endIndex = findEndBlock(startIndex = cmd.next!!)
            blocks += Block1(startIndex = cmd, endIndex = endIndex, loop = false)
            cmd = cmd.next
          }

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

          is BlockStart.IF -> {
            val endIndex = findEndBlock(startIndex = cmd.next!!)
            val elseBlock = findElseBlock(
              startIndex = cmd.next!!,
              endIndex = endIndex,
            )
            if (stack.popI32() > 0) {
              blocks += Block1(startIndex = cmd, endIndex = endIndex, loop = false)
              cmd = cmd.next
            } else {
              if (elseBlock == null) {
                cmd = endIndex.next
              } else {
                blocks += Block1(startIndex = elseBlock, endIndex = endIndex, loop = false)
                cmd = elseBlock.next
              }
            }
          }

          is ControlFlow.ELSE -> {
            cmd = blocks.removeLast().endIndex.next
          }

          is Br.BR_IF -> {
            if (stack.popI32() > 0) {
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

          is LocalIndexArgument -> cmd = LocalIndexArgumentRunner.run(
            cmd = cmd,
            stack = stack,
            args = args,
            locals = locals,
          )

          is ControlFlow.RETURN -> {
            if (stack.size < results.size) {
              throw IllegalStateException("functionId=$functionId, stack.size=${stack.size}, results.size=${results.size}")
            }
            break
          }

          is ControlFlow.UNREACHABLE -> {
            TODO("UNREACHABLE")
          }

          is ControlFlow.NOP -> {
            cmd = cmd.next
          }

          is Call.ById -> {
            callFunction(cmd.id, stack)
            cmd = cmd.next
          }

          is Call.Indirect -> {
            val tableIndex = stack.popI32()
//            val type = module.typeSection[cmd.type].single!!.single!!.type as RecType.FuncType
            val type = types[cmd.type].single!!.type as RecType.FuncType
            val table = tables[cmd.table.id.toInt()] as Table.FuncTable
            val functionId = table[tableIndex]
              ?: TODO("Function with index $tableIndex not defined!")
            callFunction(functionId, stack)
            cmd = cmd.next
          }

          is Call.ByRef -> {
            // TODO добавить валидацию
            val func = stack.pop() as Value.Ref.RefFunction
            callFunction(func.id, stack)
            cmd = cmd.next
          }

          is GlobalIndexArgument.GET -> {
            if (cmd.id.id.toInt() in globals2.indices) {
              globals2[cmd.id.id.toInt()].putInto(stack)
            } else {
              stack.push(global3[cmd.id.id.toInt() - globals2.size].value)
            }
            cmd = cmd.next
          }

          is GlobalIndexArgument.SET -> {
            if (cmd.id.id.toInt() in globals2.indices) {
              (globals2[cmd.id.id.toInt()] as GlobalVarMutable).setFrom(stack)
            } else {
              (global3[cmd.id.id.toInt() - globals2.size] as MutableValue2).value = stack.pop()//.setFrom(stack)
            }
            cmd = cmd.next
          }

          is Select -> {
            stack.select()
            cmd = cmd.next
          }

          is SelectWithType -> {
            if (cmd.types.isEmpty()) {
              stack.select()
            } else {
              require(cmd.types.size == 1)
              val v = stack.popI32()
              val v2 = stack.pop()
              val v1 = stack.pop()
              val result = if (v != 0) v1 else v2
              stack.push(
                ValueConvert.convert(
                  value = result,
                  type = TypeDictionary.convert(cmd.types[0]),
                  typeDictionary = types,
                )
              )
            }
            cmd = cmd.next
          }

          is Drop -> {
            stack.drop()
            cmd = cmd.next
          }

          is MemoryOp.Size -> {
            val mem = memory[cmd.id.raw.toInt()]
            stack.pushI32((mem.limit / PAGE_SIZE).toInt())
            cmd = cmd.next
          }

          is MemoryOp.Grow -> {
            val size = stack.popI32()
            val mem = memory[cmd.id.raw.toInt()]
            val r = mem.grow(size.toUInt()) ?: TODO()
            stack.pushI32(r.toInt())
            cmd = cmd.next
          }

          is EndBlock -> {
            if (cmd.next != null) {
              blocks.removeLast()
            } else {
              check(stack.size == results.size) { "Invalid stack and result. resultSize=${results.size}, stack.size=${stack.size}" }
              break
            }
            cmd = cmd.next
          }

          is RefFunction -> {
            stack.push(Value.Ref.RefFunction(cmd.id))
            cmd = cmd.next
          }

          is Ref.Null -> {
            stack.push(Value.Ref.NULL)
            cmd = cmd.next
          }

          is RefAsNonNull -> {
            val s = stack.peek()
            require(s is Value.Ref.Array || s is Value.Ref.RefFunction || s is Instance)
            cmd = cmd.next
          }

          is ArrayCopy -> {
            if (cmd.from == cmd.to) {
              val n = stack.pop() as Value.Primitive.I32
              val s = stack.pop() as Value.Primitive.I32
              val source = stack.pop() as Value.Ref.Array
              val d = stack.pop() as Value.Primitive.I32
              val dest = stack.pop() as Value.Ref.Array
              repeat(n.value) {
                dest.values[d.value + it] = source.values[s.value + it]
              }
//              val newArray = Value.Ref.Array(type = arr.type, values = ArrayList(arr.values))
//              ValueHistory.self.add(newArray, "Копия", mapOf("from" to arr))
//              stack.push(newArray)
            } else {
              TODO()
            }
            cmd = cmd.next
          }

          is Ref.Cast -> {
            cmd = cmd.next
          }

          is StructNew -> {
            val t = types.getRType(cmd.structTypeId) as RType.Ref.Object
            val fields = t.fields.map {
              stack.pop()
            }
            stack.push(
              Instance.create(
                type = t,
                fields = fields.asReversed()
              )
            )
            cmd = cmd.next
          }

          is StructOp.GC_STRUCT_GET -> {
            val struct = stack.pop() as Instance
            val tt = types.getRType(cmd.type) as RType.Ref.Object
            if (!types.isParent(type = struct.type, parent = tt)) {
              TODO()
            }
            stack.push(struct.fields[cmd.field.id.toInt()].value)
            cmd = cmd.next
          }

          is StructOp.GC_STRUCT_GET_S -> {
            val struct = stack.pop() as Instance
            require(types.getRType(cmd.type) === struct.type)
            stack.push(NumberUtils.getS(struct.fields[cmd.field.id.toInt()].value))
            cmd = cmd.next
          }

          is StructOp.GC_STRUCT_GET_U -> {
            val struct = stack.pop() as Instance
            require(types.getRType(cmd.type) === struct.type)
            stack.push(NumberUtils.getU(struct.fields[cmd.field.id.toInt()].value))
            cmd = cmd.next
          }

          is StructOp.GC_STRUCT_SET -> {
            val value = stack.pop()
            val struct = stack.pop() as Instance
            val field = struct.fields[cmd.field.id.toInt()]
            require(field is Instance.MutableField)
            field.value = value
            cmd = cmd.next
          }

          is Ref.CastNull -> {
            stack.push(stack.pop())
            cmd = cmd.next
          }

          is RefIsNull -> {
            val e = stack.pop()
            if (e == Value.Ref.NULL) {
              stack.pushI32(1)
            } else {
              stack.pushI32(0)
            }
            cmd = cmd.next
          }

          is ArrayLen -> {
            val array = stack.pop() as Value.Ref.Array
            stack.pushI32(array.values.size)
            cmd = cmd.next
          }

          is ThrowTag -> {
            throw WasmException(stack.pop())
          }

          is Ref.Test -> {
            val instance = stack.pop() as Instance
            val e = TypeDictionary.convert(cmd.heap, false) as VType.Ref
            val refType = types.getRType(e.id) as RType.Ref.Object
            val result = types.isParent(
              type = instance.type,
              parent = refType
            )
            stack.pushI32(if (result) 1 else 0)
            cmd = cmd.next
          }

          else -> TODO("Unknown ${cmd::class} ")
        }
      }
    } catch (e: Throwable) {
      throw RuntimeException("Can't execute task. Function ${functionId}", e)
    }
    return results.map {
      stack.pop()
    }
  }
}
