package pw.binom.tar

import pw.binom.asUTF8String
import pw.binom.fromBytes
import pw.binom.io.IOException
import pw.binom.io.InputStream

private const val BLOCK_SIZE = 512u

class TarInputStream(private val stream: InputStream) : InputStream {
    enum class Type {
        REGULAR,
        NORMAL,
        HARDLINK,
        SYMLINK,
        CHAR,
        BLOCK,
        DIRECTORY,
        FIFO,
        CONTIGUOUS
    }

    interface Entity {
        val name: String
        val size: UInt
        val uid: Long
        val gid: Long
        val type: Type
    }

    private class EntityImpl(
            override val name: String,
            override val size: UInt,
            override val uid: Long,
            override val gid: Long,
            override val type: Type
    ) : Entity

    override fun read(data: ByteArray, offset: Int, length: Int): Int {
        val entity = currentEntity ?: throw IllegalStateException("No Active Entity")
        val maxLength = minOf(length.toUInt(), entity.size - cursor)
        if (maxLength == 0u)
            return 0
        val read = stream.read(data, offset, maxLength.toInt())
        cursor += read.toUInt()
        return read
    }

    override fun skip(length: Long): Long {
        val entity = currentEntity ?: throw IllegalStateException("No Active Entity")
        val maxLength = minOf(length.toUInt(), entity.size - cursor)
        if (maxLength == 0u)
            return 0L

        val read = stream.skip(maxLength.toLong())
        cursor += read.toUInt()
        return read
    }

    override fun close() {
        stream.close()
    }

    private var currentEntity: EntityImpl? = null
    private var cursor = 0u

    private var end = false

    private fun ByteArray.oct2ToUInt(startIndex: Int = 0, length: Int = size - startIndex): UInt {
        val oct = this
        var out = 0u
        var i = startIndex
        while ((i < startIndex + length) && oct[i] != 0.toByte()) {
            out = (out shl 3) or (oct[i++] - '0'.toByte()).toUInt()
        }
        return out
    }

    private fun ByteArray.isZeroOnly() = indexOfFirst { it != 0.toByte() } == -1
    private val header = ByteArray(BLOCK_SIZE.toInt())
    fun getNextEntity(): Entity? {
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
                    stream.skip((needForRead).toLong())
                }
            }
        }
        stream.read(header)
        if (header.isZeroOnly()) {
            stream.skip(BLOCK_SIZE.toLong())
            end = true
            return null
        }
        val nameSize1 = header.indexOfFirst { it == 0.toByte() }

        var name = header.asUTF8String(length = nameSize1)
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
        currentEntity = EntityImpl(
                name = name,
                size = size,
                uid = Long.fromBytes(
                        header[108],
                        header[109],
                        header[110],
                        header[111],
                        header[112],
                        header[113],
                        header[114],
                        header[115]
                ),
                gid = Long.fromBytes(
                        header[116],
                        header[117],
                        header[118],
                        header[119],
                        header[120],
                        header[121],
                        header[122],
                        header[123]
                ),
                type = when (typeNum) {
                    47.toByte() -> Type.REGULAR
                    48.toByte() -> Type.NORMAL
                    49.toByte() -> Type.HARDLINK
                    50.toByte() -> Type.SYMLINK
                    51.toByte() -> Type.CHAR
                    52.toByte() -> Type.BLOCK
                    53.toByte() -> Type.DIRECTORY
                    54.toByte() -> Type.FIFO
                    55.toByte() -> Type.CONTIGUOUS
                    else -> throw IOException("Unknown Entity Type $typeNum")
                }
        )
        cursor = 0u
        return currentEntity!!
    }
}