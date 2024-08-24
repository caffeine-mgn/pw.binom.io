package pw.binom.wasm.readers

import pw.binom.wasm.StreamReader
import pw.binom.wasm.visitors.ExportSectionVisitor

object ExportSectionReader {
  const val FUNC: UByte = 0x00u
  const val TABLE: UByte = 0x01u
  const val MEM: UByte = 0x02u
  const val GLOBAL: UByte = 0x03u
  fun read(input: StreamReader, visitor: ExportSectionVisitor) {
    visitor.start()
    input.readVec {
      val name = input.readString()
      when (val type = input.readUByte()) {
        FUNC -> visitor.func(name, input.v32u())
        TABLE -> visitor.table(name, input.v32u())
        MEM -> visitor.memory(name, input.v32u())
        GLOBAL -> visitor.global(name, input.v32u())
        else -> TODO("Unknown export type: 0x${type.toString(16).padStart(2, '0')}")
      }
    }
    visitor.end()
  }
}
