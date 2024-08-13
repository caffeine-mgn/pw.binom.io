package pw.binom.wasm

object DataSectionReader {
  private const val TYPE0: UByte = 0u
  private const val TYPE1: UByte = 1u
  private const val TYPE2: UByte = 2u

  fun read(input: StreamReader) {
    input.readVec {
      when (val type = input.readUByte()) {
        TYPE0 -> {
          ExpressionReader.readExpressions(input = input, visitor = ExpressionsVisitor.STUB)
          input.skip(input.v32u().toInt()) // data
        }

        TYPE1 -> {
          input.skip(input.v32u().toInt()) // data
        }
        TYPE2 -> {
          input.v32u()
          ExpressionReader.readExpressions(input = input, visitor = ExpressionsVisitor.STUB)
          input.skip(input.v32u().toInt()) // data
        }
        else -> TODO("Unknown type: $type")
      }
    }
  }
}
