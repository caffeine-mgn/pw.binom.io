package pw.binom.wasm.text.writers

import pw.binom.wasm.*
import pw.binom.wasm.visitors.ExpressionsVisitor
import pw.binom.wasm.visitors.ValueVisitor

class TextExpressionsVisitor(
  val sb: Appendable,
  val printPadding: Boolean = true,
) : ExpressionsVisitor {
  private var level = 1

  private fun printPadding() {
    if (!printPadding) {
      return
    }
    repeat(level) {
      sb.append("  ")
    }
  }

  override fun afterOperation() {
    sb.appendLine()
  }

  override fun const(value: Float) {
    printPadding()
    sb.append("f32.const ").append(value.toString())
  }

  override fun const(value: Double) {
    printPadding()
    sb.append("f64.const ").append(value.toString())
  }

  override fun const(value: Int) {
    printPadding()
    sb.append("i32.const ").append(value.toString())
  }

  override fun const(value: Long) {
    printPadding()
    sb.append("i64.const ").append(value.toString())
  }

  override fun call(function: FunctionId) {
    printPadding()
    sb.append("call ").append(function.id.toString())
  }

  override fun drop() {
    printPadding()
    sb.append("drop")
  }

  override fun select() {
    printPadding()
    sb.append("select")
  }

  override fun indexArgument(opcode: UByte, value: LocalId) {
    printPadding()
    val opName = when (opcode) {
      Opcodes.GET_LOCAL -> "local.get"
      Opcodes.SET_LOCAL -> "local.set"
      Opcodes.TEE_LOCAL -> "local.tee"
      else -> TODO()
    }
    sb.append(opName).append(" ").append(value.id.toString())
  }

  override fun ref(function: FunctionId) {
    printPadding()
    sb.append("ref.func ").append(function.id.toString())
  }

  override fun indexArgument(opcode: UByte, value: GlobalId) {
    printPadding()
    val opName = when (opcode) {
      Opcodes.GET_GLOBAL -> "global.get"
      Opcodes.SET_GLOBAL -> "global.set"
      else -> TODO()
    }
    sb.append(opName).append(" ").append(value.id.toString())
    super.indexArgument(opcode, value)
  }

  override fun convertNumeric(numOpcode: UByte) {
    printPadding()
    TODO()
  }

  override fun structNew(type: TypeId) {
    printPadding()
    sb.append("struct.new ").append(type.value.toString())
  }

  override fun structNewDefault(type: TypeId) {
    printPadding()
    sb.append("struct.new_default ").append(type.value.toString())
  }

  override fun refIsNull() {
    printPadding()
    sb.append("ref.is_null")
  }

  override fun structOp(gcOpcode: UByte, type: TypeId, field: FieldId) {
    printPadding()
    val opName = when (gcOpcode) {
      Opcodes.GC_STRUCT_SET -> "struct.set"
      Opcodes.GC_STRUCT_GET -> "struct.get"
      Opcodes.GC_STRUCT_GET_S -> "struct.get_s"
      Opcodes.GC_STRUCT_GET_U -> "struct.get_u"
      else -> TODO()
    }
    sb.append(opName).append(" ").append(type.value.toString()).append(" ").append(field.id.toString())
  }

  override fun compare(opcode: UByte) {
    printPadding()
    val opName = when (opcode) {
      Opcodes.I32_EQZ -> "i32.eqz"
      Opcodes.I32_EQ -> "i32.eq"
      Opcodes.I32_NE -> "i32.ne"
      Opcodes.I32_LT_S -> "i32.lt_s"
      Opcodes.I32_LT_U -> "i32.lt_u"
      Opcodes.I32_GT_S -> "i32.gt_s"
      Opcodes.I32_GT_U -> "i32.gt_u"
      Opcodes.I32_LE_S -> "i32.le_s"
      Opcodes.I32_LE_U -> "i32.le_u"
      Opcodes.I32_GE_S -> "i32.ge_s"
      Opcodes.I32_GE_U -> "i32.ge_u"
      Opcodes.I64_EQZ -> "i64.eqz"
      Opcodes.I64_EQ -> "i64.eq"
      Opcodes.I64_NE -> "i64.ne"
      Opcodes.I64_LT_S -> "i64.lt_s"
      Opcodes.I64_LT_U -> "i64.lt_u"
      Opcodes.I64_GT_S -> "i64.gt_s"
      Opcodes.I64_GT_U -> "i64.gt_u"
      Opcodes.I64_LE_S -> "i64.le_s"
      Opcodes.I64_LE_U -> "i64.le_u"
      Opcodes.I64_GE_S -> "i64.ge_s"
      Opcodes.I64_GE_U -> "i64.ge_u"
      Opcodes.F32_EQ -> "f32.eq"
      Opcodes.F32_NE -> "f32.ne"
      Opcodes.F32_LT -> "f32.lt"
      Opcodes.F32_GT -> "f32.gt"
      Opcodes.F32_LE -> "f32.le"
      Opcodes.F32_GE -> "f32.ge"
      Opcodes.F64_EQ -> "f64.eq"
      Opcodes.F64_NE -> "f64.ne"
      Opcodes.F64_LT -> "f64.lt"
      Opcodes.F64_GT -> "f64.gt"
      Opcodes.F64_LE -> "f64.le"
      Opcodes.F64_GE -> "f64.ge"
      Opcodes.REF_EQ -> "ref.eq"
      else -> TODO()
    }
    sb.append(opName)
  }

  override fun throwOp(tag: TagId) {
    printPadding()
    sb.append("throw ").append(tag.value.toString())
  }

  override fun numeric(opcode: UByte) {
    printPadding()
    val opName = when (opcode) {
      Opcodes.I32_CLZ -> "i32.clz"
      Opcodes.I32_CTZ -> "i32.ctz"
      Opcodes.I32_POPCNT -> "i32.popcnt"
      Opcodes.I32_ADD -> "i32.add"
      Opcodes.I32_SUB -> "i32.sub"
      Opcodes.I32_MUL -> "i32.mul"
      Opcodes.I32_DIV_S -> "i32.div_s"
      Opcodes.I32_DIV_U -> "i32.div_u"
      Opcodes.I32_REM_S -> "i32.rem_s"
      Opcodes.I32_REM_U -> "i32.rem_u"
      Opcodes.I32_AND -> "i32.and"
      Opcodes.I32_OR -> "i32.or"
      Opcodes.I32_XOR -> "i32.xor"
      Opcodes.I32_SHL -> "i32.shl"
      Opcodes.I32_SHR_S -> "i32.shr_s"
      Opcodes.I32_SHR_U -> "i32.shr_u"
      Opcodes.I32_ROTL -> "i32.rotl"
      Opcodes.I32_ROTR -> "i32.rotr"
      Opcodes.I64_CLZ -> "i64.clz"
      Opcodes.I64_CTZ -> "i64.ctz"
      Opcodes.I64_POPCNT -> "i64.popcnt"
      Opcodes.I64_ADD -> "i64.add"
      Opcodes.I64_SUB -> "i64.sub"
      Opcodes.I64_MUL -> "i64.mul"
      Opcodes.I64_DIV_S -> "i64.div_s"
      Opcodes.I64_DIV_U -> "i64.div_u"
      Opcodes.I64_REM_S -> "i64.rem_s"
      Opcodes.I64_REM_U -> "i64.rem_u"
      Opcodes.I64_AND -> "i64.and"
      Opcodes.I64_OR -> "i64.or"
      Opcodes.I64_XOR -> "i64.xor"
      Opcodes.I64_SHL -> "i64.shl"
      Opcodes.I64_SHR_S -> "i64.shr_s"
      Opcodes.I64_SHR_U -> "i64.shr_u"
      Opcodes.I64_ROTL -> "i64.rotl"
      Opcodes.I64_ROTR -> "i64.rotr"
      Opcodes.F32_ABS -> "f32.abs"
      Opcodes.F32_NEG -> "f32.neg"
      Opcodes.F32_CEIL -> "f32.ceil"
      Opcodes.F32_FLOOR -> "f32.floor"
      Opcodes.F32_TRUNC -> "f32.trunc"
      Opcodes.F32_NEAREST -> "f32.nearest"
      Opcodes.F32_SQRT -> "f32.sqrt"
      Opcodes.F32_ADD -> "f32.add"
      Opcodes.F32_SUB -> "f32.sub"
      Opcodes.F32_MUL -> "f32.mul"
      Opcodes.F32_DIV -> "f32.div"
      Opcodes.F32_MIN -> "f32.min"
      Opcodes.F32_MAX -> "f32.max"
      Opcodes.F32_COPYSIGN -> "f32.copysign"
      Opcodes.F64_ABS -> "f64.abs"
      Opcodes.F64_NEG -> "f64.neg"
      Opcodes.F64_CEIL -> "f64.ceil"
      Opcodes.F64_FLOOR -> "f64.floor"
      Opcodes.F64_TRUNC -> "f64.trunc"
      Opcodes.F64_NEAREST -> "f64.nearest"
      Opcodes.F64_SQRT -> "f64.sqrt"
      Opcodes.F64_ADD -> "f64.add"
      Opcodes.F64_SUB -> "f64.sub"
      Opcodes.F64_MUL -> "f64.mul"
      Opcodes.F64_DIV -> "f64.div"
      Opcodes.F64_MIN -> "f64.min"
      Opcodes.F64_MAX -> "f64.max"
      Opcodes.F64_COPYSIGN -> "f64.copysign"
      else -> TODO()
    }
    sb.append(opName)
    super.numeric(opcode)
  }

  override fun selectWithType(): ExpressionsVisitor.SelectVisitor {
    sb.append("select (result ")
    return object : ExpressionsVisitor.SelectVisitor {
      override fun type(): ValueVisitor {
        return TextValueVisitor(sb)
      }

      override fun end() {
        sb.append(")")
      }
    }
  }

  override fun memory(opcode: UByte, align: UInt, offset: UInt, memoryId: MemoryId) {
    val opName = when (opcode) {
      Opcodes.I32_LOAD -> "i32.load"
      Opcodes.I64_LOAD -> "i64.load"
      Opcodes.F32_LOAD -> "f32.load"
      Opcodes.F64_LOAD -> "f64.load"
      Opcodes.I32_LOAD8_S -> "i32.load8_s"
      Opcodes.I32_LOAD8_U -> "i32.load8_u"
      Opcodes.I32_LOAD16_S -> "i32.load16_s"
      Opcodes.I32_LOAD16_U -> "i32.load16_u"
      Opcodes.I64_LOAD8_S -> "i64.load8_s"
      Opcodes.I64_LOAD8_U -> "i64.load8_u"
      Opcodes.I64_LOAD16_S -> "i64.load16_s"
      Opcodes.I64_LOAD16_U -> "i64.load16_u"
      Opcodes.I64_LOAD32_S -> "i64.load32_s"
      Opcodes.I64_LOAD32_U -> "i64.load32_u"
      Opcodes.I32_STORE -> "i32.store"
      Opcodes.I64_STORE -> "i64.store"
      Opcodes.F32_STORE -> "f32.store"
      Opcodes.F64_STORE -> "f64.store"
      Opcodes.I32_STORE8 -> "i32.store8"
      Opcodes.I32_STORE16 -> "i32.store16"
      Opcodes.I64_STORE8 -> "i64.store8"
      Opcodes.I64_STORE16 -> "i64.store16"
      Opcodes.I64_STORE32 -> "i64.store32"
      else -> TODO()
    }
    printPadding()
    sb.append(opName)
      .append(" ")
      .append("align=").append(align.toString())
    if (offset != 0u) {
      sb.append(" offset=").append(offset.toString())
    }
    if (memoryId.raw != 0u) {
      sb.append(" memory=").append(memoryId.raw.toString())
    }
  }

  override fun catch(v32u: UInt) {
    level--
    printPadding()
    sb.append("catch ").append(v32u.toString())
    level++
  }

  override fun newArray(type: TypeId, data: DataId) {
    printPadding()
    sb.append("array.new_data ").append(type.value.toString())
      .append(" ").append(data.id.toString())
  }

  override fun newArray(type: TypeId, size: UInt) {
    printPadding()
    sb.append("array.new_fixed ").append(type.value.toString())
      .append(" ").append(size.toString())
  }

  override fun callIndirect(type: TypeId, table: TableId) {
    sb.append("call_indirect ").append(type.value.toString())
      .append(" ").append(table.id.toString())
  }

  override fun br(opcode: UByte, label: LabelId) {
    printPadding()
    val opName = when (opcode) {
      Opcodes.BR -> "br"
      Opcodes.BR_IF -> "br_if"
      Opcodes.BR_ON_NULL -> "br_no_null"
      Opcodes.BR_ON_NON_NULL -> "br_on_non_null"
      else -> TODO()
    }
    sb.append(opName).append(" ").append(label.id.toString())
      .append(" (;@").append((level - label.id.toInt() - 1).toString()).append(";)")
    super.br(opcode, label)
  }

  override fun startBlock(opcode: UByte): ExpressionsVisitor.BlockStartVisitor {
    printPadding()
    val blockName = when (opcode) {
      Opcodes.LOOP -> "loop"
      Opcodes.BLOCK -> "block"
      Opcodes.TRY -> "try"
      Opcodes.IF -> "if"
      else -> TODO()
    }
    level++
    sb.append(blockName).append(" ")
    val o = TextBlockStartVisitor(sb)
    return object : ExpressionsVisitor.BlockStartVisitor by o {
      private var valueExist = false
      override fun valueType(): ValueVisitor {
        sb.append("(result ")
        valueExist = true
        return o.valueType()
      }

      override fun end() {
        o.end()
        if (valueExist) {
          sb.append(") ")
        }
        sb.append(";; label = @").append((level - 1).toString())
        super.end()
      }
    }
  }

  override fun newArrayDefault(type: TypeId) {
    printPadding()
    sb.append("array.new_default ").append(type.value.toString())
  }

  override fun arrayLen() {
    printPadding()
    sb.append("array.len")
  }

  override fun convert(opcode: UByte) {
    printPadding()
    val opName = when (opcode) {
      Opcodes.I32_WRAP_I64 -> "i32.wrap/i64"
      Opcodes.I32_TRUNC_S_F32 -> "i32.trunc_s/f32"
      Opcodes.I32_TRUNC_U_F32 -> "i32.trunc_u/f32"
      Opcodes.I32_TRUNC_S_F64 -> "i32.trunc_s/f64"
      Opcodes.I32_TRUNC_U_F64 -> "i32.trunc_u/f64"
      Opcodes.I64_EXTEND_S_I32 -> "i64.extend_s/i32"
      Opcodes.I64_EXTEND_U_I32 -> "i64.extend_u/i32"
      Opcodes.I64_TRUNC_S_F32 -> "i64.trunc_s/f32"
      Opcodes.I64_TRUNC_U_F32 -> "i64.trunc_u/f32"
      Opcodes.I64_TRUNC_S_F64 -> "i64.trunc_s/f64"
      Opcodes.I64_TRUNC_U_F64 -> "i64.trunc_u/f64"
      Opcodes.F32_CONVERT_S_I32 -> "f32.convert_s/i32"
      Opcodes.F32_CONVERT_U_I32 -> "f32.convert_u/i32"
      Opcodes.F32_CONVERT_S_I64 -> "f32.convert_s/i64"
      Opcodes.F32_CONVERT_U_I64 -> "f32.convert_u/i64"
      Opcodes.F32_DEMOTE_F64 -> "f32.demote/f64"
      Opcodes.F64_CONVERT_S_I32 -> "f64.convert_s/i32"
      Opcodes.F64_CONVERT_U_I32 -> "f64.convert_u/i32"
      Opcodes.F64_CONVERT_S_I64 -> "f64.convert_s/i64"
      Opcodes.F64_CONVERT_U_I64 -> "f64.convert_u/i64"
      Opcodes.F64_PROMOTE_F32 -> "f64.promote/f32"
      Opcodes.I32_EXTEND8_S -> "i32.extend8_s"
      Opcodes.I32_EXTEND16_S -> "i32.extend16_s"
      Opcodes.I64_EXTEND8_S -> "i64.extend8_s"
      Opcodes.I64_EXTEND16_S -> "i64.extend16_s"
      Opcodes.I64_EXTEND32_S -> "i64.extend32_s"
      else -> TODO()
    }
    sb.append(opName)
  }

  override fun controlFlow(opcode: UByte) {
    when (opcode) {
      Opcodes.UNREACHABLE -> {
        printPadding()
        sb.append("unreachable")
      }

      Opcodes.NOP -> {
        printPadding()
        sb.append("nop")
      }

      Opcodes.ELSE -> {
        level--
        printPadding()
        sb.append("else")
        level++
      }

      Opcodes.RETURN -> {
        printPadding()
        sb.append("return")
      }

      else -> TODO()
    }
  }

  override fun call(typeRef: TypeId) {
    printPadding()
    sb.append("call_ref ").append(typeRef.value.toString())
  }

  override fun ref(gcOpcode: UByte): ValueVisitor.HeapVisitor {
    printPadding()
    val opName = when (gcOpcode) {
      Opcodes.GC_REF_CAST -> "ref.cast"
      Opcodes.GC_REF_TEST_NULL -> "ref.test"
      Opcodes.GC_REF_TEST -> "ref.test"
      Opcodes.GC_REF_CAST_NULL -> "ref.cast"
      else -> TODO()
    }
    sb.append(opName).append(" ")
    val nullable = when (gcOpcode) {
      Opcodes.GC_REF_CAST,
      Opcodes.GC_REF_TEST,
        -> false

      Opcodes.GC_REF_TEST_NULL,
      Opcodes.GC_REF_CAST_NULL,
        -> true

      else -> TODO()
    }
    return TextHeapVisitor(sb = sb, nullable = nullable)
  }

  override fun arrayCopy(from: TypeId, to: TypeId) {
    printPadding()
    sb.append("array.copy ").append(from.value.toString()).append(" ").append(to.value.toString())
  }

  override fun refNull(): ValueVisitor.HeapVisitor {
    printPadding()
    sb.append("ref.null ")
    return TextHeapVisitor(sb = sb, nullable = false)
  }

  override fun arrayOp(gcOpcode: UByte, type: TypeId) {
    printPadding()
    val opName = when (gcOpcode) {
      Opcodes.GC_ARRAY_GET -> "array.get"
      Opcodes.GC_ARRAY_SET -> "array.set"
      Opcodes.GC_ARRAY_GET_S -> "array.get_s"
      Opcodes.GC_ARRAY_GET_U -> "array.get_u"
      else -> TODO()
    }
    sb.append(opName).append(" ").append(type.value.toString())
  }

  override fun endBlock() {
    level--
    printPadding()
    sb.append("end")
  }

  override fun end() {
    require(level == 0)
    sb.appendLine(")")
  }
}
