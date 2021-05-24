package pw.binom.compression.tar

import pw.binom.*
import pw.binom.io.Closeable
import pw.binom.io.IOException
import pw.binom.io.StreamClosedException

internal const val BLOCK_SIZE = 512

internal fun ByteBuffer.oct2ToUInt(startIndex: Int = 0, length: Int = capacity - startIndex): UInt {
    val oct = this
    var out = 0u
    var i = startIndex
    while ((i < startIndex + length) && oct[i] != 0.toByte()) {
        out = (out shl 3) or (oct[i++] - '0'.code.toByte()).toUInt()
    }
    return out
}

class TarReader(private val stream: Input) : Closeable {

    inner class TarEntity(
        val name: String,
        val size: UInt,
        val uid: UInt,
        val gid: UInt,
        val type: TarEntityType,
        val mode: UInt,
        val time: Long
    ) : Input {
        override fun read(dest: ByteBuffer): Int {
            if (currentEntity != this)
                throw StreamClosedException()
            val entity = this
            val maxLength = minOf(dest.remaining, entity.size.toInt() - cursor)
            if (maxLength == 0)
                return 0
            val read = dest.set(dest.position, maxLength) { stream.read(it) }
            cursor += read
            return read
        }

        override fun close() {
        }
    }

    private var currentEntity: TarEntity? = null
    private var cursor = 0
    private val tmp = ByteBuffer.alloc(128)

    fun Input.skip(length: Int) {
        var l = length
        while (l > 0) {
            tmp.reset(0, minOf(tmp.capacity, l))
            l -= read(tmp)
        }
    }

    private var end = false

    private fun ByteBuffer.isZeroOnly() = indexOfFirst { it != 0.toByte() } == -1
    private fun ByteBuffer.indexOfFirst(func: (Byte) -> Boolean): Int {
        (position until limit).forEach {
            if (func(this[it]))
                return@indexOfFirst it
        }
        return -1
    }

    private val header = ByteBuffer.alloc(BLOCK_SIZE)

    fun getNextEntity(): TarEntity? {
        if (end) {
            return null
        }
        val entity = currentEntity
        if (entity != null) {
            var fullSize = (entity.size / BLOCK_SIZE.toUInt()) * BLOCK_SIZE.toUInt()
            if (entity.size % BLOCK_SIZE.toUInt() > 0u)
                fullSize += BLOCK_SIZE.toUInt()
            if (cursor.toUInt() < fullSize) {
                val needForRead = fullSize - cursor.toUInt()
                if (needForRead > 0u) {
                    val needForSkip = needForRead
                    stream.skip(needForSkip.toInt())
                }
            }
        }
        header.clear()
        stream.read(header)
        header.flip()
        if (header.isZeroOnly()) {
            stream.skip(BLOCK_SIZE)
            end = true
            return null
        }
        val nameSize = header.indexOfFirst { it == 0.toByte() }
        var name = header.set(header.position, nameSize) {
            it.asUTF8String()
        }
        var size = header.oct2ToUInt(124, 12)
        var typeNum = header[156]
        if (typeNum == 76.toByte()) {
            var fullSize = size / BLOCK_SIZE.toUInt() * BLOCK_SIZE.toUInt()
            if (size % BLOCK_SIZE.toUInt() > 0u)
                fullSize += BLOCK_SIZE.toUInt()
            ByteBuffer.alloc(size.toInt() - 1) { nameBuf ->
                stream.read(nameBuf)
                stream.skip((fullSize - size).toInt() + 1)
                nameBuf.flip()
                name = nameBuf.asUTF8String()
                nameBuf.close()
            }
            stream.read(header)
            size = header.oct2ToUInt(124, 12)
            typeNum = header[156]
        }
        val mode = header.oct2ToUInt(100, 8)
        val uid = header.oct2ToUInt(108, 8)
        val gid = header.oct2ToUInt(116, 8)
        val time = header.set(136, 11) {
            it.asUTF8String()
        }.toLong()
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
        cursor = 0
        return currentEntity!!
    }

    override fun close() {
        tmp.close()
        header.close()
    }
}