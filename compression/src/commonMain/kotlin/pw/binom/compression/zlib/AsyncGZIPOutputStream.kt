package pw.binom.compression.zlib

import pw.binom.io.AsyncOutputStream
import pw.binom.io.CRC32

internal const val GZIP_MAGIC = 0x8b1f
internal const val GZIP_MAGIC1 = 0x1f.toByte()
internal const val GZIP_MAGIC2 = 0x8b.toByte()
internal const val TRAILER_SIZE = 8
internal const val DEFLATED = 8.toByte()

@Deprecated(level = DeprecationLevel.WARNING, message = "Use Input/Output")
class AsyncGZIPOutputStream(stream: AsyncOutputStream, level: Int, bufferSize: Int = 512) : AsyncDeflaterOutputStream(
        stream = stream,
        bufferSize = bufferSize,
        level = level,
        wrap = false,
        syncFlush = false
) {
    private val crc = CRC32()

    init {
        crc.reset()
        usesDefaultDeflater = false
    }

    override suspend fun write(data: ByteArray, offset: Int, length: Int): Int {
        writeHeader()
        val r = super.write(data, offset, length)
        crc.update(data, offset, length)
        return r
    }

    override suspend fun finish() {
        writeHeader()
        super.finish()
        val trailer = ByteArray(TRAILER_SIZE)
        writeTrailer(trailer, 0)
        stream.write(trailer)
    }

    private fun writeTrailer(buf: ByteArray, offset: Int) {
        writeInt(crc.value.toInt(), buf, offset) // CRC-32 of uncompr. data
        writeInt(def.totalIn.toInt(), buf, offset + 4) // Number of uncompr. bytes
    }

    private fun writeInt(i: Int, buf: ByteArray, offset: Int) {

        writeShort(i and 0xffff, buf, offset)
        writeShort(i shr 16 and 0xffff, buf, offset + 2)
    }

    /*
     * Writes short integer in Intel byte order to a byte array, starting
     * at a given offset
     */
    private fun writeShort(s: Int, buf: ByteArray, offset: Int) {
        buf[offset] = (s and 0xff).toByte()
        buf[offset + 1] = (s shr 8 and 0xff).toByte()
    }

    private var headerWrited = false
    private suspend fun writeHeader() {
        if (headerWrited)
            return
        stream.write(byteArrayOf(
                GZIP_MAGIC1,  // Magic number (short)
                GZIP_MAGIC2,  // Magic number (short)
                DEFLATED,  // Compression method (CM)
                0,  // Flags (FLG)
                0,  // Modification time MTIME (int)
                0,  // Modification time MTIME (int)
                0,  // Modification time MTIME (int)
                0,  // Modification time MTIME (int)
                0,  // Extra flags (XFLG)
                0 // Operating system (OS)
        ))
        headerWrited = true
    }
}