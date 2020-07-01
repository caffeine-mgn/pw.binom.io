package pw.binom.compression.zlib

import pw.binom.AsyncOutput
import pw.binom.ByteBuffer
import pw.binom.io.CRC32

class AsyncGZIPOutput(stream: AsyncOutput, level: Int, bufferSize: Int = 512,autoCloseStream: Boolean = false) : AsyncDeflaterOutput(
        stream = stream,
        bufferSize = bufferSize,
        level = level,
        wrap = false,
        syncFlush = false,
        autoCloseStream=autoCloseStream
) {
    private val crc = CRC32()

    init {
        crc.reset()
        usesDefaultDeflater = false
    }

//    override suspend fun write(data: ByteDataBuffer, offset: Int, length: Int): Int {
//        writeHeader()
//        val r = super.write(data, offset, length)
//        crc.update(data, offset, length)
//        return r
//    }

    override suspend fun write(data: ByteBuffer): Int {
        writeHeader()
        val pos = data.position
        val r = super.write(data)
        crc.update(data, pos, r)
        return r
    }

    override suspend fun finish() {
        writeHeader()
        super.finish()
        val trailer = ByteBuffer.alloc(TRAILER_SIZE)
        writeTrailer(trailer)
        trailer.flip()
        stream.write(trailer)
    }

    private fun writeTrailer(buf: ByteBuffer) {
        writeInt(crc.value.toInt(), buf) // CRC-32 of uncompr. data
        writeInt(def.totalIn.toInt(), buf) // Number of uncompr. bytes
    }

    private fun writeInt(i: Int, buf: ByteBuffer) {

        writeShort(i and 0xffff, buf)
        writeShort(i shr 16 and 0xffff, buf)
    }

    /*
     * Writes short integer in Intel byte order to a byte array, starting
     * at a given offset
     */
    private fun writeShort(s: Int, buf: ByteBuffer) {
        buf.put((s and 0xff).toByte())
        buf.put((s shr 8 and 0xff).toByte())
    }

    private var headerWrited = false
    private suspend fun writeHeader() {
        if (headerWrited)
            return
        header.clear()
        stream.write(header)
        headerWrited = true
    }
}

private val header = ByteBuffer.alloc(10).also {
    it.put(GZIP_MAGIC1)// Magic number (short)
    it.put(GZIP_MAGIC2)  // Magic number (short)
    it.put(DEFLATED)  // Compression method (CM)
    it.put(0)  // Flags (FLG)
    it.put(0)  // Modification time MTIME (int)
    it.put(0)  // Modification time MTIME (int)
    it.put(0)  // Modification time MTIME (int)
    it.put(0)  // Modification time MTIME (int)
    it.put(0)  // Extra flags (XFLG)
    it.put(0) // Operating system (OS)
}