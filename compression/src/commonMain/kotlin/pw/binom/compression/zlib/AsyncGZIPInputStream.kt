package pw.binom.compression.zlib

import pw.binom.io.*

class AsyncGZIPInputStream(stream: AsyncInputStream, bufferSize: Int = 512) : AsyncInflateInputStream(stream, bufferSize, false) {
    private val crc = CRC32()
    private val tmpbuf = ByteArray(128)

    init {
        usesDefaultInflater = true
    }

    override suspend fun read(data: ByteArray, offset: Int, length: Int): Int {
        readHeader(stream)
        return super.read(data, offset, length)
    }

    private var headerRead = false
    private suspend fun readHeader(stream: AsyncInputStream): Int {
        if (headerRead)
            return 0
        headerRead = true
        crc.reset()
        val stream = AsyncCheckedInputStream(stream, crc)
        val b1 = stream.read().toUByte()
        val b2 = stream.read().toUByte()
        if (b1 != 0x1fu.toUByte() || b2 != 0x8bu.toUByte())
            throw IOException("Not in GZIP format")
        // Check compression method
        if (stream.read() != DEFLATED) {
            throw IOException("Unsupported compression method")
        }
        // Read flags
        val flg: Int = readUByte(stream)
        // Skip MTIME, XFL, and OS fields
        skipBytes(stream, 6)
        var n = 2 + 2 + 6
        // Skip optional extra field
        if (flg and FEXTRA == FEXTRA) {
            val m: Int = readUShort(stream)
            skipBytes(stream, m)
            n += m + 2
        }
        // Skip optional file name
        if (flg and FNAME == FNAME) {
            do {
                n++
            } while (readUByte(stream) != 0)
        }
        // Skip optional file comment
        if (flg and FCOMMENT == FCOMMENT) {
            do {
                n++
            } while (readUByte(stream) != 0)
        }
        // Check optional header CRC
        if (flg and FHCRC == FHCRC) {
            val v = crc.value.toInt() and 0xffff
            if (readUShort(stream) != v) {
                throw IOException("Corrupt GZIP header")
            }
            n += 2
        }
        crc.reset()
        return n
    }

    private suspend fun skipBytes(stream: AsyncInputStream, n: Int) {
        var n = n
        while (n > 0) {
            val len: Int = stream.read(tmpbuf, 0, if (n < tmpbuf.size) n else tmpbuf.size)
            if (len == -1) {
                throw EOFException()
            }
            n -= len
        }
    }

    private suspend fun readUShort(stream: AsyncInputStream): Int {
        val b: Int = readUByte(stream)
        return readUByte(stream) shl 8 or b
    }

    private suspend fun readUByte(stream: AsyncInputStream): Int {
        val b: Int = stream.read().toInt()
        if (b == -1) {
            throw EOFException()
        }
        if (b < -1 || b > 255) {
            // Report on this.in, not argument in; see read{Header, Trailer}.
            throw IOException("read() returned value out of range -1..255: " + b)
        }
        return b
    }
}