package pw.binom.wasm

import pw.binom.wasm.visitors.ExpressionsVisitor

class EV(val out: ExpressionsVisitor) : ExpressionsVisitor by out {
  override fun const(value: Int) {
    val resultValue = when (value) {
      424444 -> 1
      424442224 -> 2
      else -> value
    }
    out.const(resultValue)
  }
}
