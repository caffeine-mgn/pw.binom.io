package pw.binom.wasm.visitors

import pw.binom.wasm.FunctionId

interface WasmVisitor {
  fun start() {}
  fun end() {}
  fun version(version: Int) {}
  fun importSection(): ImportSectionVisitor = ImportSectionVisitor.STUB
  fun functionSection(): FunctionSectionVisitor = FunctionSectionVisitor.STUB
  fun codeVisitor(): CodeSectionVisitor = CodeSectionVisitor.STUB
  fun typeSection(): TypeSectionVisitor = TypeSectionVisitor.STUB
  fun customSection(): CustomSectionVisitor = CustomSectionVisitor.STUB
  fun startSection(function: FunctionId) {}
  fun tableSection(): TableSectionVisitor = TableSectionVisitor.SKIP
  fun memorySection(): MemorySectionVisitor = MemorySectionVisitor.SKIP
  fun globalSection(): GlobalSectionVisitor = GlobalSectionVisitor.SKIP
  fun tagSection(): TagSectionVisitor = TagSectionVisitor.SKIP
  fun exportSection(): ExportSectionVisitor = ExportSectionVisitor.SKIP
  fun dataCountSection(): DataCountSectionVisitor = DataCountSectionVisitor.SKIP
  fun dataSection(): DataSectionVisitor = DataSectionVisitor.SKIP
}
