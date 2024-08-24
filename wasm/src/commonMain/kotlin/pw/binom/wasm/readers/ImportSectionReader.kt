package pw.binom.wasm.readers

import pw.binom.wasm.FunctionId
import pw.binom.wasm.WasmInput
import pw.binom.wasm.readRefType
import pw.binom.wasm.readVec
import pw.binom.wasm.visitors.ImportSectionVisitor

internal object ImportSectionReader {

  private fun readTable(
    input: WasmInput,
    module: String,
    field: String,
    visitor: ImportSectionVisitor,
  ) {
    val tableVisitor = visitor.table(
      module = module,
      field = field,
    )
    tableVisitor.start()
    input.readRefType(visitor = tableVisitor.type())
    val limitExist = input.v1u()
    val min = input.v32u()

    if (limitExist) {
      tableVisitor.range(min = min, max = input.v32u())
    } else {
      tableVisitor.range(min = min)
    }
    tableVisitor.end()
  }

  private fun readMemory(module: String, field: String, input: WasmInput, visitor: ImportSectionVisitor) {
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

  fun readImportSection(input: WasmInput, visitor: ImportSectionVisitor) {
    visitor.start()
    input.readVec {
      val module = input.string()
      val field = input.string()
      val kind = input.sByte()
      when (kind) {
        0.toByte() -> visitor.function(
          module = module,
          field = field,
          index = FunctionId(input.v32u())
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
    }
    visitor.end()
//    input.skip(length.toLong())
  }
}
