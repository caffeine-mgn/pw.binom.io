package pw.binom.wasm.readers

import pw.binom.wasm.StreamReader
import pw.binom.wasm.visitors.MemorySectionVisitor

object MemorySectionReader {
  fun read(input: StreamReader, visitor: MemorySectionVisitor) {
    visitor.start()
    input.readVec {
      input.readLimit(min = { min ->
        visitor.memory(inital = min)
      }, range = { min, max ->
        visitor.memory(inital = min, max = max)
      })
    }
    visitor.end()
  }
}
