package pw.binom.wasm.node.inst

import pw.binom.wasm.visitors.ExpressionsVisitor

sealed class Inst {
  abstract fun accept(visitor: ExpressionsVisitor)
  var next: Inst? = null
}
