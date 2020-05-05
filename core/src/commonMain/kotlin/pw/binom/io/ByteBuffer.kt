package pw.binom.io

import pw.binom.ByteDataBuffer
import pw.binom.Stack

class ByteBuffer(private val packageSize: Int) : Closeable {
    var readRemaining = 0
        private set

    private inner class Package {
        val data = ByteDataBuffer.alloc(packageSize)
        private var writePosition = 0
        private var readPosition = 0

        fun write(data: ByteArray, offset: Int, length: Int): Int {
            val len = minOf(writeRemaining, length)
            if (len == 0)
                return 0
            for (i in 0 until len) {
                this.data[writePosition + i] = data[i + offset]
            }
            this@ByteBuffer.readRemaining += len
            writePosition += len
            return len
        }

        fun read(data: ByteArray, offset: Int, length: Int): Int {
            val len = minOf(readRemaining, length)
            if (len == 0)
                return 0
            for (i in offset until len) {
                data[i + offset] = this.data[readPosition + i]
            }
            readPosition += len
            this@ByteBuffer.readRemaining -= len
            return len
        }

        val writeRemaining
            get() = data.size - writePosition

        val readRemaining
            get() = data.size - readPosition
    }

    private val packages = Stack<Package>()

    private fun getReadyForWrite(): Package {
        if (packages.isEmpty) {
            val p = Package()
            packages.pushLast(p)
            return p
        }
        var last = packages.peekLast()
        if (last.writeRemaining > 0)
            return last
        last = Package()
        packages.pushLast(last)
        return last
    }

    private fun getReadyForRead(): Package? {
        while (true) {
            if (packages.isEmpty)
                return null
            val l = packages.peekFirst()
            if (l.readRemaining <= 0)
                packages.popFirst()
            return l
        }
    }

    fun write(data: ByteArray, offset: Int = 0, length: Int = data.size - offset) {
        if (length == 0)
            return
        var index = offset
        var len = length
        while (len > 0) {
            val p = getReadyForWrite()
            val w = p.write(data, index, len)
            index += w
            len -= w
            if (w == 0) throw IllegalArgumentException()
        }
    }

    fun read(data: ByteArray, offset: Int = 0, length: Int = data.size - offset): Int {
        if (length == 0)
            return 0
        var index = offset
        var len = length
        var read = 0
        while (len > 0) {
            val p = getReadyForRead() ?: return read
            val r = p.read(data, index, len)
            read += r

            index += r
            len -= r
        }
        return read
    }

    override fun close() {
        while (!packages.isEmpty) {
            packages.popFirst().data.close()
        }
    }
}