package pw.binom.compression.zlib

internal const val GZIP_MAGIC = 0x8b1f
internal const val GZIP_MAGIC1 = 0x1f.toByte()
internal const val GZIP_MAGIC2 = 0x8b.toByte()
internal const val TRAILER_SIZE = 8
internal const val DEFLATED = 8.toByte()


internal const val FTEXT = 1 // Extra text
internal const val FHCRC = 2 // Header CRC
internal const val FEXTRA = 4 // Extra field
internal const val FNAME = 8 // File name
internal const val FCOMMENT = 16 // File comment