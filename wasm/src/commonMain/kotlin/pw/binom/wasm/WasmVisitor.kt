package pw.binom.wasm

import pw.binom.wasm.visitors.*

interface WasmVisitor {
  fun start() {}
  fun end() {}
  fun version(version: Int) {}
  fun importSection(): ImportSectionVisitor = ImportSectionVisitor.Companion.STUB
  fun functionSection(): FunctionSectionVisitor = FunctionSectionVisitor.STUB
  fun codeVisitor(): CodeSectionVisitor = CodeSectionVisitor.Companion.STUB
  fun typeSection(): TypeSectionVisitor = TypeSectionVisitor.STUB
  fun customSection(): CustomSectionVisitor = CustomSectionVisitor.Companion.STUB
  fun startSection(function: FunctionId) {}
  fun tableSection(): TableSectionVisitor = TableSectionVisitor.SKIP
  fun memorySection(): MemorySectionVisitor = MemorySectionVisitor.SKIP
  fun globalSection(): GlobalSectionVisitor = GlobalSectionVisitor.SKIP
  fun exportSection(): ExportSectionVisitor = ExportSectionVisitor.SKIP
  fun dataCountSection(): DataCountSectionVisitor = DataCountSectionVisitor.SKIP
}
