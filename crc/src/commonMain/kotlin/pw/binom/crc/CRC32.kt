package pw.binom.crc

open class CRC32 : CRC32Basic(init = 0u, crcTable = table) {
  companion object {
    internal val table = makeCrcTable(0xEDB88320U)
  }
}
