package pw.binom.wasm.readers

import pw.binom.io.use
import pw.binom.wasm.MemoryId
import pw.binom.wasm.WasmInput
import pw.binom.wasm.readVec
import pw.binom.wasm.visitors.DataSectionVisitor

/**
 * https://webassembly.github.io/gc/core/binary/modules.html#binary-datasec
 */
object DataSectionReader {
  const val ACTIVE_MEM_0: UByte = 0u
  const val PASSIVE: UByte = 1u
  const val ACTIVE_MEM_X: UByte = 2u

  fun read(input: WasmInput, visitor: DataSectionVisitor) {
    visitor.start()
    input.readVec {
      visitor.elementStart()
      when (val type = input.i8u()) {
        ACTIVE_MEM_0 -> {
          ExpressionReader.readExpressions(input = input, visitor = visitor.active())
          val size = input.v32u()
          input.withLimit(size).use { l ->
            visitor.data(l)
            l.skipOther()
          }
        }

        PASSIVE -> {
          visitor.passive()
          val size = input.v32u()
          input.withLimit(size).use { l ->
            visitor.data(l)
            l.skipOther()
          }
        }

        ACTIVE_MEM_X -> {
          val v = visitor.active(memoryId = MemoryId(input.v32u()))
          ExpressionReader.readExpressions(input = input, visitor = v)
          val size = input.v32u()
          input.withLimit(size).use { l ->
            visitor.data(l)
            l.skipOther()
          }
        }

        else -> TODO("Unknown type: $type")
      }
      visitor.elementEnd()
    }
    visitor.end()
  }
}
