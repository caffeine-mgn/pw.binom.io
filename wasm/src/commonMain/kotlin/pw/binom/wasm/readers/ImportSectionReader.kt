package pw.binom.wasm.readers

import pw.binom.wasm.*
import pw.binom.wasm.visitors.ImportSectionVisitor
import pw.binom.wasm.visitors.ValueVisitor

/**
 * https://webassembly.github.io/gc/core/binary/modules.html#binary-importsec
 */
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

  private fun readGlobal(module: String, field: String, input: WasmInput, visitor: ImportSectionVisitor){
    val v = visitor.global(
      module = module,
      field = field,
    )
    v.start()
    input.readValueType(visitor= v.type())
    v.mutable(input.v1u())
    v.end()
  }

  fun readImportSection(input: WasmInput, visitor: ImportSectionVisitor) {
    input as StreamReader
    println("START IMPORT ON 0x${input.globalCursor.toString(16)}")
    visitor.start()
    input.readVec {
      val module = input.string()
      val field = input.string()
      val kind = input.i8s()
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

        3.toByte() -> readGlobal(
          input = input,
          visitor = visitor,
          module = module,
          field = field,
        )
        4.toByte() -> TODO("tag")
        else -> TODO("Unknown import kind  $kind (0x${kind.toUByte().toString(16)})")
      }
    }
    visitor.end()
//    input.skip(length.toLong())
  }
}
