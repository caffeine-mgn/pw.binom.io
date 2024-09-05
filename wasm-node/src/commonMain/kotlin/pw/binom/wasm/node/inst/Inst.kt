package pw.binom.wasm.node.inst

import pw.binom.wasm.visitors.ExpressionsVisitor

sealed interface Inst {
  fun accept(visitor: ExpressionsVisitor)
}
