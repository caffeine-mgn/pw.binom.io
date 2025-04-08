package pw.binom.wasm.text.writers

import pw.binom.wasm.AbsoluteHeapType
import pw.binom.wasm.visitors.ValueVisitor

class TextRefVisitor(val sb: Appendable) : ValueVisitor.RefVisitor {
  override fun ref(): ValueVisitor.HeapVisitor =
    TextHeapVisitor(sb = sb, nullable = false)

  override fun refNull(): ValueVisitor.HeapVisitor =
    TextHeapVisitor(sb = sb, nullable = true)

  override fun refNull(type: AbsoluteHeapType) {
    TODO()
  }
}
