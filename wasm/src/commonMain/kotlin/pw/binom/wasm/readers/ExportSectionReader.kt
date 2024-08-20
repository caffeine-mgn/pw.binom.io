package pw.binom.wasm.readers

import pw.binom.wasm.StreamReader

object ExportSectionReader {
  private const val FUNC: UByte = 0x00u
  private const val TABLE: UByte = 0x01u
  private const val MEM: UByte = 0x02u
  private const val GLOBAL: UByte = 0x03u
  fun read(input: StreamReader) {
    val name = input.readString()
    println("export name: $name")
    when (val type = input.readUByte()) {
      FUNC -> input.v32u()
      TABLE -> input.v32u()
      MEM -> input.v32u()
      GLOBAL -> input.v32u()
      else -> TODO("Unknown export type: 0x${type.toString(16).padStart(2, '0')}")
    }
  }
}
