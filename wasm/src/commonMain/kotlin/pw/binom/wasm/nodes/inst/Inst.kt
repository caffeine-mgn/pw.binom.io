package pw.binom.wasm.nodes.inst

import pw.binom.wasm.visitors.ExpressionsVisitor

sealed interface Inst {
  fun accept(visitor: ExpressionsVisitor)
}
