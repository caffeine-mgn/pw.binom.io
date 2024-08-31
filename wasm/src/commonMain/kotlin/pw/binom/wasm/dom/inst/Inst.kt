package pw.binom.wasm.dom.inst

import pw.binom.wasm.visitors.ExpressionsVisitor

sealed interface Inst {
  fun accept(visitor: ExpressionsVisitor)
}
