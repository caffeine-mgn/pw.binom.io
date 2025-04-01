package pw.binom.wasm.node.inst

import pw.binom.wasm.Opcodes
import pw.binom.wasm.node.ValueType
import pw.binom.wasm.visitors.ExpressionsVisitor
import pw.binom.wasm.visitors.ValueVisitor
import kotlin.js.JsName

sealed class BlockStart : Inst(), ExpressionsVisitor.BlockStartVisitor {
  @JsName("valueTypeF")
  abstract var valueType: ValueType?

  override fun start() {
    super.start()
  }

  override fun withoutType() {
    valueType = null
  }

  override fun valueType(): ValueVisitor {
    val e = ValueType()
    valueType = e
    return e
  }

  override fun end() {
    super.end()
  }

  class LOOP : BlockStart() {
    override var valueType: ValueType? = null
    override fun accept(visitor: ExpressionsVisitor) {
      val v = visitor.startBlock(Opcodes.LOOP)
      v.start()
      val valueType = valueType
      if (valueType == null) {
        v.withoutType()
      } else {
        valueType.accept(v.valueType())
      }
      v.end()
    }
  }

  class BLOCK : BlockStart() {
    override var valueType: ValueType? = null
    override fun toString(): String = "BLOCK(valueType=$valueType)"
    override fun accept(visitor: ExpressionsVisitor) {
      val v = visitor.startBlock(Opcodes.BLOCK)
      v.start()
      val valueType = valueType
      if (valueType == null) {
        v.withoutType()
      } else {
        valueType.accept(v.valueType())
      }
      v.end()
    }
  }

  class TRY : BlockStart() {
    override var valueType: ValueType? = null
    override fun accept(visitor: ExpressionsVisitor) {
      val v = visitor.startBlock(Opcodes.TRY)
      v.start()
      val valueType = valueType
      if (valueType == null) {
        v.withoutType()
      } else {
        valueType.accept(v.valueType())
      }
      v.end()
    }
  }

  class IF : BlockStart() {
    override var valueType: ValueType? = null
    override fun accept(visitor: ExpressionsVisitor) {
      val v = visitor.startBlock(Opcodes.IF)
      v.start()
      val valueType = valueType
      if (valueType == null) {
        v.withoutType()
      } else {
        valueType.accept(v.valueType())
      }
      v.end()
    }
  }
}
