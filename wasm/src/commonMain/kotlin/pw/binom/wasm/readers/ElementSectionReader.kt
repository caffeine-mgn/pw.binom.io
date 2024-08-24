package pw.binom.wasm.readers

import pw.binom.wasm.visitors.ExpressionsVisitor
import pw.binom.wasm.StreamReader

object ElementSectionReader {
  private const val TYPE0: UByte = 0u
  private const val TYPE1: UByte = 1u
  private const val TYPE2: UByte = 2u
  private const val TYPE3: UByte = 3u
  private const val TYPE4: UByte = 4u
  private const val TYPE5: UByte = 5u
  private const val TYPE6: UByte = 6u
  private const val TYPE7: UByte = 7u
  fun read(input: StreamReader) {
    input.readVec {
      println("SECTION!!!")
      when (val type = input.readUByte()) {
        TYPE0 -> {
          ExpressionReader.readExpressions(input = input, visitor = ExpressionsVisitor.Companion.SKIP)
          input.readVec {
            input.v32u()
          }
        }

        else -> TODO("Unknown type: $type")
      }
    }
  }
}
