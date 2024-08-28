package pw.binom.wasm.readers

import pw.binom.wasm.FunctionId
import pw.binom.wasm.visitors.ExpressionsVisitor
import pw.binom.wasm.WasmInput
import pw.binom.wasm.readVec
import pw.binom.wasm.visitors.ElementSectionVisitor

object ElementSectionReader {
  const val TYPE0: UByte = 0u
  private const val TYPE1: UByte = 1u
  private const val TYPE2: UByte = 2u
  private const val TYPE3: UByte = 3u
  private const val TYPE4: UByte = 4u
  private const val TYPE5: UByte = 5u
  private const val TYPE6: UByte = 6u
  private const val TYPE7: UByte = 7u
  fun read(input: WasmInput, visitor: ElementSectionVisitor) {
    visitor.start()
    input.readVec {
      when (val type = input.i8u()) {
        TYPE0 -> {
          val e = visitor.type0()
          ExpressionReader.readExpressions(input = input, visitor = e.exp())
          input.readVec {
            e.func(FunctionId(input.v32u()))
          }
        }

        else -> TODO("Unknown type: $type")
      }
    }
    visitor.end()
  }
}
