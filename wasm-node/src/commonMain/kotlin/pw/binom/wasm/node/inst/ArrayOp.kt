package pw.binom.wasm.node.inst

import pw.binom.wasm.DataId
import pw.binom.wasm.Opcodes
import pw.binom.wasm.TypeId
import pw.binom.wasm.visitors.ExpressionsVisitor


sealed class ArrayOp : Inst() {
  abstract val type: TypeId


  data class NewByDefault(override val type: TypeId) : ArrayOp() {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.newArrayDefault(type)
    }

    override fun toString() = "array.new_default ${type.value}"
  }

  data class NewByData(override val type: TypeId, var data: DataId) : ArrayOp() {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.newArray(type = type, data = data)
    }

    override fun toString() = "array.new_data ${type.value} ${data.id}"
  }

  data class NewBySize(override val type: TypeId, var size: UInt) : ArrayOp() {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.newArray(type = type, size = size)
    }
  }

  data class Get(override val type: TypeId) : ArrayOp() {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.arrayOp(gcOpcode = Opcodes.GC_ARRAY_GET, type = type)
    }

    override fun toString(): String = "array.get ${type.value}"

  }

  data class Set(override val type: TypeId) : ArrayOp() {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.arrayOp(gcOpcode = Opcodes.GC_ARRAY_SET, type = type)
    }

    override fun toString(): String = "array.set ${type.value}"
  }

  data class GetS(override val type: TypeId) : ArrayOp() {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.arrayOp(gcOpcode = Opcodes.GC_ARRAY_GET_S, type = type)
    }

    override fun toString(): String = "array.get_s ${type.value}"
  }

  data class GetU(override val type: TypeId) : ArrayOp() {
    override fun accept(visitor: ExpressionsVisitor) {
      visitor.arrayOp(gcOpcode = Opcodes.GC_ARRAY_GET_U, type = type)
    }

    override fun toString(): String = "array.get_u ${type.value}"
  }
}
