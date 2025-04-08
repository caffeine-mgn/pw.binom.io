package pw.binom.wasm.text.writers

import pw.binom.wasm.AbsoluteHeapType
import pw.binom.wasm.visitors.ValueVisitor

class TextValueVisitor(val sb: Appendable) : ValueVisitor {
  override fun numType(): ValueVisitor.NumberVisitor =
    TextNumberVisitor(sb)

  override fun refType(): ValueVisitor.RefVisitor =
    TextRefVisitor(sb)

  override fun refType(type: AbsoluteHeapType) {
    sb.append(type.code)
  }

  override fun vecType(): ValueVisitor.VectorVisitor {
    TODO()
  }
}
