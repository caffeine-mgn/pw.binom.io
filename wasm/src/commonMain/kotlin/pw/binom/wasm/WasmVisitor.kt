package pw.binom.wasm

interface WasmVisitor {
  fun start()
  fun end()
  fun version(version: Int) {}
  fun importSection(count: Int): ImportSectionVisitor
  fun functionSection(count: Int): FunctionSectionVisitor
  fun codeVisitor(count: Int): CodeSectionVisitor
  fun typeSection(count: Int): TypeSectionVisitor
  fun customSection(): CustomSectionVisitor
}
