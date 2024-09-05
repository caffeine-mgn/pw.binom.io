package pw.binom.wasm.node

import pw.binom.wasm.Vector
import pw.binom.wasm.visitors.ValueVisitor

class VectorType(var type: Vector) : ValueVisitor.VectorVisitor {

  override fun v128() {
    type = Vector.V128
  }

  fun accept(visitor: ValueVisitor.VectorVisitor) {
    visitor.v128()
  }
}
