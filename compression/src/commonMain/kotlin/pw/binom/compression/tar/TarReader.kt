package pw.binom.compression.tar

import pw.binom.asUTF8String
import pw.binom.io.IOException
import pw.binom.io.InputStream
import pw.binom.io.StreamClosedException

internal const val BLOCK_SIZE = 512u

internal fun ByteArray.oct2ToUInt(startIndex: Int = 0, length: Int = size - startIndex): UInt {
    val oct = this
    var out = 0u
    var i = startIndex
    while ((i < startIndex + length) && oct[i] != 0.toByte()) {
        out = (out shl 3) or (oct[i++] - '0'.toByte()).toUInt()
    }
    return out
}

class TarReader(private val stream: InputStream) {

    inner class TarEntity(
            val name: String,
            val size: UInt,
            val uid: UInt,
            val gid: UInt,
            val type: TarEntityType,
            val mode: UInt,
            val time:Long
    ):InputStream{
        override fun read(data: ByteArray, offset: Int, length: Int): Int {
            if (currentEntity!=this)
                throw StreamClosedException()
            val entity = this
            val maxLength = minOf(length.toUInt(), entity.size - cursor)
            if (maxLength == 0u)
                return 0
            val read = stream.read(data, offset, maxLength.toInt())
            cursor += read.toUInt()
            return read
        }

        override fun close() {
        }

        override fun skip(length: Long): Long {
            if (currentEntity!=this)
                throw StreamClosedException()
            val entity = this
            val maxLength = minOf(length.toUInt(), entity.size - cursor)
            if (maxLength == 0u)
                return 0L

            val read = stream.skip(maxLength.toLong())
            cursor += read.toUInt()
            return read
        }
    }

    private var currentEntity: TarEntity? = null
    private var cursor = 0u

    private var end = false

    private fun ByteArray.isZeroOnly() = indexOfFirst { it != 0.toByte() } == -1
    private val header = ByteArray(BLOCK_SIZE.toInt())

    @OptIn(ExperimentalStdlibApi::class)
    fun getNextEntity(): TarEntity? {
        if (end)
            return null
        val entity = currentEntity
        if (entity != null) {
            var fullSize = (entity.size / BLOCK_SIZE) * BLOCK_SIZE
            if (entity.size % BLOCK_SIZE > 0u)
                fullSize += BLOCK_SIZE
            if (cursor < fullSize) {
                val needForRead = fullSize - cursor
                if (needForRead > 0u) {
                    val needForSkip = needForRead.toLong()
                    if (stream.skip(needForSkip) != needForSkip)
                        throw IllegalStateException("Can't skip a part of Tar Stream")
                }
            }
        }
        stream.read(header)
        if (header.isZeroOnly()) {
            stream.skip(BLOCK_SIZE.toLong())
            end = true
            return null
        }
        val nameSize = header.indexOfFirst { it == 0.toByte() }

        var name = header.asUTF8String(length = nameSize)
        var size = header.oct2ToUInt(124, 12)
        var typeNum = header[156]
        if (typeNum == 76.toByte()) {
            var fullSize = size / BLOCK_SIZE * BLOCK_SIZE
            if (size % BLOCK_SIZE > 0u)
                fullSize += BLOCK_SIZE
            val nameBuf = ByteArray(size.toInt() - 1)
            stream.read(nameBuf)
            stream.skip((fullSize - size).toLong() + 1)
            name = nameBuf.asUTF8String()
            stream.read(header)
            size = header.oct2ToUInt(124, 12)
            typeNum = header[156]
        }
        val mode = header.oct2ToUInt(100, 8)
        val uid = header.oct2ToUInt(108, 8)
        val gid = header.oct2ToUInt(116, 8)
        val time = header.decodeToString(136, 136 + 11).toLong()
        val chksum = header.oct2ToUInt(148, 8)
        currentEntity = TarEntity(
                name = name,
                size = size,
                uid = uid,
                gid = gid,
                mode = mode,
                time = time,
                type = TarEntityType.findByCode(typeNum) ?: throw IOException("Unknown Entity Type $typeNum")
        )
        cursor = 0u
        return currentEntity!!
    }
}