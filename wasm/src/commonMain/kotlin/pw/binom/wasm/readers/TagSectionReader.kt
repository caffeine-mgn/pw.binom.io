package pw.binom.wasm.readers

import pw.binom.wasm.TypeId
import pw.binom.wasm.WasmInput
import pw.binom.wasm.readVec
import pw.binom.wasm.visitors.TagSectionVisitor

object TagSectionReader {
  fun read(input: WasmInput, visitor: TagSectionVisitor) {
    visitor.start()
    input.readVec {
      check(input.i8u() == 0u.toUByte())
      visitor.tag(TypeId(input.v32u()))
    }
    visitor.end()
  }
}
