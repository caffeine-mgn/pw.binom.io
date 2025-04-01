package pw.binom.wasm.runner.stack

import pw.binom.wasm.node.ValueType

abstract class AbstractStack : Stack {
  protected abstract fun popAny():Any
}
