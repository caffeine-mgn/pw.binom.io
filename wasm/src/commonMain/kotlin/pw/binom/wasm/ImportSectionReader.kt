package pw.binom.wasm

internal object ImportSectionReader {

  private fun readTable(
    input: StreamReader,
    module: String,
    field: String,
    visitor: ImportSectionVisitor,
  ) {
    val refType = input.readRefType()
    val limitExist = input.v1u()
    val min = input.v32u()
    if (limitExist) {
      visitor.table(
        module = module,
        field = field,
        min = min,
        max = input.v32u(),
        type = refType,
      )
    } else {
      visitor.table(
        module = module,
        field = field,
        min = min,
        type = refType,
      )
    }

  }

  private fun readMemory(module: String, field: String, input: StreamReader, visitor: ImportSectionVisitor) {
    val maximumExist = input.v1u()
    val initial = input.v32u()
    if (maximumExist) {
      visitor.memory(
        module = module,
        field = field,
        initial = initial,
        maximum = input.v32u(),
      )
    } else {
      visitor.memory(
        module = module,
        field = field,
        initial = initial,
      )
    }
  }

  fun readImportSection(input: StreamReader, visitor: ImportSectionVisitor) {
    visitor.start()
    val module = input.readString()
    val field = input.readString()
    val kind = input.readByte()
    when (kind) {
      0.toByte() -> visitor.function(
        module = module,
        field = field,
        index = input.v32u()
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
