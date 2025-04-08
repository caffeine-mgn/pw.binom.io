package pw.binom.wasm.text.writers

import pw.binom.wasm.AbsoluteHeapType
import pw.binom.wasm.TypeId
import pw.binom.wasm.visitors.ValueVisitor

class TextHeapVisitor(val sb: Appendable, val nullable: Boolean) : ValueVisitor.HeapVisitor {
  override fun type(type: AbsoluteHeapType) {
    sb.append(type.code)
  }

  override fun type(type: TypeId) {
    sb.append("(ref ")
    if (nullable) {
      sb.append("null ")
    }
    sb.append(type.value.toString())
    sb.append(")")
  }
}
