package pw.binom.wasm.node

import pw.binom.io.ByteArrayInput
import pw.binom.io.use
import pw.binom.wasm.visitors.CustomSectionVisitor

data class CustomBlock(var name: String, val data: ByteArray) {
  fun accept(visitor: CustomSectionVisitor) {
    visitor.start(name = name)
    ByteArrayInput(data).use { dataInput ->
      visitor.data(dataInput)
    }
    visitor.end()
  }
}
