package pw.binom.wasm.text.writers

import pw.binom.wasm.visitors.ExpressionsVisitor.BlockStartVisitor
import pw.binom.wasm.visitors.ValueVisitor

class TextBlockStartVisitor(val sb: Appendable): BlockStartVisitor {
  override fun valueType(): ValueVisitor = TextValueVisitor(sb)

  override fun withoutType() {
  }
}
