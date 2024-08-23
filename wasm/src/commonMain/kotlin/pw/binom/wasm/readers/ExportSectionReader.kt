package pw.binom.wasm.readers

import pw.binom.wasm.StreamReader
import pw.binom.wasm.visitors.ExportSectionVisitor

object ExportSectionReader {
  const val FUNC: UByte = 0x00u
  const val TABLE: UByte = 0x01u
  const val MEM: UByte = 0x02u
  const val GLOBAL: UByte = 0x03u
  fun read(input: StreamReader, visitor: ExportSectionVisitor) {
    val name = input.readString()
    visitor.start(name)
    when (val type = input.readUByte()) {
      FUNC -> visitor.func(input.v32u())
      TABLE -> visitor.table(input.v32u())
      MEM -> visitor.memory(input.v32u())
      GLOBAL -> visitor.global(input.v32u())
      else -> TODO("Unknown export type: 0x${type.toString(16).padStart(2, '0')}")
    }
    visitor.end()
  }
}
