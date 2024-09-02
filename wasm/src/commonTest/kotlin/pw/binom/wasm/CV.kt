package pw.binom.wasm

import pw.binom.wasm.visitors.CodeSectionVisitor
import pw.binom.wasm.writers.CodeSectionWriter

class CV(val out: CodeSectionVisitor.CodeVisitor) : CodeSectionVisitor.CodeVisitor by out {
  override fun code() = EV(out.code())
}
