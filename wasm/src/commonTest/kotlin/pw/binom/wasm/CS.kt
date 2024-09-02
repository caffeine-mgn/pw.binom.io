package pw.binom.wasm

import pw.binom.wasm.visitors.CodeSectionVisitor
import pw.binom.wasm.writers.CodeSectionWriter

class CS(val out: CodeSectionVisitor) : CodeSectionVisitor by out {
  override fun code() = CV(out.code())
}
