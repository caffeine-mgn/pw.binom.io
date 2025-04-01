package pw.binom.wasm.text.writers

import pw.binom.wasm.visitors.ValueVisitor

class TextNumberVisitor(val sb:Appendable): ValueVisitor.NumberVisitor {
  override fun i32() {
    sb.append("i32")
  }

  override fun i64() {
    sb.append("i64")
  }

  override fun f32() {
    sb.append("f32")
  }

  override fun f64() {
    sb.append("f64")
  }
}
