package pw.binom.compression.zlib

import pw.binom.AsyncOutput
import pw.binom.ByteDataBuffer
import pw.binom.io.AsyncOutputStream
import pw.binom.io.CRC32

class AsyncGZIPOutput(stream: AsyncOutput, level: Int, bufferSize: Int = 512) : AsyncDeflaterOutput(
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

    override suspend fun write(data: ByteDataBuffer, offset: Int, length: Int): Int {
        writeHeader()
        val r = super.write(data, offset, length)
        crc.update(data, offset, length)
        return r
    }

    override suspend fun finish() {
        writeHeader()
        super.finish()
        val trailer = ByteDataBuffer.alloc(TRAILER_SIZE)
        writeTrailer(trailer, 0)
        stream.write(trailer)
    }

    private fun writeTrailer(buf: ByteDataBuffer, offset: Int) {
        writeInt(crc.value.toInt(), buf, offset) // CRC-32 of uncompr. data
        writeInt(def.totalIn.toInt(), buf, offset + 4) // Number of uncompr. bytes
    }

    private fun writeInt(i: Int, buf: ByteDataBuffer, offset: Int) {

        writeShort(i and 0xffff, buf, offset)
        writeShort(i shr 16 and 0xffff, buf, offset + 2)
    }

    /*
     * Writes short integer in Intel byte order to a byte array, starting
     * at a given offset
     */
    private fun writeShort(s: Int, buf: ByteDataBuffer, offset: Int) {
        buf[offset] = (s and 0xff).toByte()
        buf[offset + 1] = (s shr 8 and 0xff).toByte()
    }

    private var headerWrited = false
    private suspend fun writeHeader() {
        if (headerWrited)
            return
        stream.write(header)
        headerWrited = true
    }
}

private val header = ByteDataBuffer.alloc(10).also {
    it[0]=GZIP_MAGIC1  // Magic number (short)
    it[1]=GZIP_MAGIC2  // Magic number (short)
    it[2]=DEFLATED  // Compression method (CM)
    it[3]=0  // Flags (FLG)
    it[4]=0  // Modification time MTIME (int)
    it[5]=0  // Modification time MTIME (int)
    it[6]=0  // Modification time MTIME (int)
    it[7]=0  // Modification time MTIME (int)
    it[8]=0  // Extra flags (XFLG)
    it[9]=0 // Operating system (OS)
}