package pw.binom.wasm

import pw.binom.wasm.visitors.CodeSectionVisitor
import pw.binom.wasm.visitors.CustomSectionVisitor
import pw.binom.wasm.visitors.FunctionSectionVisitor
import pw.binom.wasm.visitors.ImportSectionVisitor
import pw.binom.wasm.visitors.TypeSectionVisitor

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
}
