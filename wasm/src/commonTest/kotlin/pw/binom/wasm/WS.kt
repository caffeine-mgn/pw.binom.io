package pw.binom.wasm

import pw.binom.wasm.visitors.WasmVisitor
import pw.binom.wasm.writers.CodeSectionWriter
import pw.binom.wasm.writers.WasmWriter

class WS(val out: WasmVisitor) : WasmVisitor by out {
  override fun codeSection() = CS(out.codeSection())
//  override fun codeSection() = CodeSectionWriter(sectionData)
}
