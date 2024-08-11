package pw.binom.wasm

interface FunctionSectionVisitor {
  fun start()
  fun value(int:Int)
  fun end()
}
