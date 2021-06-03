package pw.binom.compression.zlib

import pw.binom.ByteBuffer
import pw.binom.Output
import pw.binom.io.CRC32
import pw.binom.io.use

class GZIPOutput(
    stream: Output,
    level: Int,
    bufferSize: Int = 1024,
    closeStream: Boolean
) : DeflaterOutput(
    stream = stream,
    bufferSize = bufferSize,
    level = level,
    wrap = false,
    syncFlush = false,
    closeStream = closeStream
) {
    private val crc = CRC32()

    init {
        crc.reset()
        usesDefaultDeflater = false
    }

    override fun write(data: ByteBuffer): Int {
        writeHeader()
        val pos = data.position
        val r = super.write(data)
        crc.update(data, pos, r)
        return r
    }

//    override fun write(data: ByteDataBuffer, offset: Int, length: Int): Int {
//        writeHeader()
//        val r = super.write(data, offset, length)
//        crc.update(data, offset, length)
//        return r
//    }

    override fun finish() {
        writeHeader()
        super.finish()

        fun writeFinal(buf: ByteBuffer) {
            buf.clear()
            writeTrailer(buf)
            buf.flip()
            stream.write(buf)
            stream.flush()
        }

        if (buf.capacity > TRAILER_SIZE) {
            writeFinal(buf)
        } else {
            ByteBuffer.alloc(TRAILER_SIZE).use { trailer ->
                writeFinal(trailer)
            }
        }
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
    private fun writeHeader() {
        if (headerWrited) {
            return
        }
        header.clear()
        stream.write(header)
        headerWrited = true
    }
}

private val header = ByteBuffer.alloc(10).also {
    it.put(GZIP_MAGIC1)  // Magic number (short)
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