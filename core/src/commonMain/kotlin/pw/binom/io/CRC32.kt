package pw.binom.io

@ExperimentalUnsignedTypes
class CRC32 : CRC32Basic(0xEDB88320U, 0u, table) {
    companion object {
        internal val table = make_crc_table(0xEDB88320U)
    }
}