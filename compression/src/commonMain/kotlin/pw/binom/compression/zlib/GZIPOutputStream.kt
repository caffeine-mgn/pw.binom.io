package pw.binom.compression.zlib

import pw.binom.io.CRC32
import pw.binom.io.OutputStream

class GZIPOutputStream(stream: OutputStream, level: Int, bufferSize: Int = 512) : DeflaterOutputStream(
        stream = stream,
        bufferSize = bufferSize,
        level = level,
        wrap = false,
        syncFlush = false
) {
    private val crc = CRC32()

    init {
        crc.reset()
        writeHeader()
        usesDefaultDeflater = false
    }

    override fun write(data: ByteArray, offset: Int, length: Int): Int {
        val r = super.write(data, offset, length)
        crc.update(data, offset, length)
        return r
    }

    override fun finish() {
        super.finish()
        val trailer = ByteArray(TRAILER_SIZE)
        writeTrailer(trailer, 0)
        stream.write(trailer)
    }

    private fun writeTrailer(buf: ByteArray, offset: Int) {
        writeInt(crc.value.toInt(), buf, offset) // CRC-32 of uncompr. data
        writeInt(deflater.totalIn.toInt(), buf, offset + 4) // Number of uncompr. bytes
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

    private fun writeHeader() {
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
    }
}