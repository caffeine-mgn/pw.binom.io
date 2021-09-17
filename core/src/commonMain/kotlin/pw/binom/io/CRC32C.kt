package pw.binom.io

class CRC32C : CRC32Basic(init = 0u, crcTable = table) {
    companion object {
        internal val table = makeCrcTable(0x82F63B78U)
    }
}