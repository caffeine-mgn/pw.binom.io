package pw.binom.wasm

object DataSectionReader {
  private const val TYPE0: UByte = 0u
  private const val TYPE1: UByte = 1u
  private const val TYPE2: UByte = 2u

  fun read(input: InputReader) {
    input.readVec {
      when (val type = input.readUByte()) {
        TYPE0 -> {
          CodeSectionReader.readExpressions(input = input, visitor = ExpressionsVisitor.STUB)
          input.skip(input.readVarUInt32L().toInt()) // data
        }

        TYPE1 -> {
          input.skip(input.readVarUInt32L().toInt()) // data
        }
        TYPE2 -> {
          input.readVarUInt32L()
          CodeSectionReader.readExpressions(input = input, visitor = ExpressionsVisitor.STUB)
          input.skip(input.readVarUInt32L().toInt()) // data
        }
        else -> TODO("Unknown type: $type")
      }
    }
  }
}
