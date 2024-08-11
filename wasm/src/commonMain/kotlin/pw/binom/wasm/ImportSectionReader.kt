package pw.binom.wasm

internal object ImportSectionReader {

  private fun readTable(
    input: InputReader,
    module: String,
    field: String,
    visitor: ImportSectionVisitor,
  ) {
    val refType = input.readRefType()
    val limitExist = input.readVarUInt1()
    val min = input.readVarUInt32AsInt()
    val max = if (limitExist) {
      input.readVarUInt32AsInt()
    } else {
      null
    }
    visitor.table(module = module, field = field, min = min, max = max, type = refType)
  }

  private fun readMemory(module: String, field: String, input: InputReader, visitor: ImportSectionVisitor) {
    val maximumExist = input.readVarUInt1()
    val initial = input.readVarUInt32AsInt()
    val maximum = if (maximumExist) input.readVarUInt32AsInt() else null
    visitor.memory(
      module = module,
      field = field,
      initial = initial,
      maximum = maximum,
    )
  }

  fun readImportSection(input: InputReader, visitor: ImportSectionVisitor) {
    visitor.start()
    val module = input.readString()
    val field = input.readString()
    val kind = input.readByte()
    when (kind) {
      0.toByte() -> visitor.function(
        module = module,
        field = field,
        index = input.readVarUInt32AsInt()
      )

      1.toByte() -> readTable(
        input = input,
        module = module,
        field = field,
        visitor = visitor,
      )

      2.toByte() -> readMemory(
        input = input,
        visitor = visitor,
        module = module,
        field = field,
      )

      3.toByte() -> TODO("global")
      4.toByte() -> TODO("tag")
      else -> TODO("Unknown import kind  $kind (0x${kind.toUByte().toString(16)})")
    }
    visitor.end()
//    input.skip(length.toLong())
  }
}
