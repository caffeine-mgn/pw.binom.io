package pw.binom.wasm

object ExportSectionReader {
  private const val FUNC: UByte = 0x00u
  private const val TABLE: UByte = 0x01u
  private const val MEM: UByte = 0x02u
  private const val GLOBAL: UByte = 0x03u
  fun read(input: InputReader) {
    val name = input.readString()
    println("export name: $name")
    when (val type = input.readUByte()) {
      FUNC -> input.readVarUInt32L()
      TABLE -> input.readVarUInt32L()
      MEM -> input.readVarUInt32L()
      GLOBAL -> input.readVarUInt32L()
      else -> TODO("Unknown export type: 0x${type.toString(16).padStart(2, '0')}")
    }
  }
}
