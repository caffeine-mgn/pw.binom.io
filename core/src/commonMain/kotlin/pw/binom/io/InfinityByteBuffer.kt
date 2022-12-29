package pw.binom.io

import pw.binom.collections.Stack
import pw.binom.set

class InfinityByteBuffer(private val packageSize: Int) : Closeable, Output, Input {
    var readRemaining = 0
        private set

    private inner class Package {
        val data = ByteBuffer(packageSize)
        private var writePosition = 0
        private var readPosition = 0

        fun write(data: ByteBuffer): Int {
            checkClosed()
            val len = minOf(writeRemaining, data.remaining)
            if (len == 0) {
                return 0
            }
//            this.data.writeTo(writePosition, data, offset, len)
            this.data.set(writePosition, len) { buf ->
                buf.write(data)
            }
//            data.writeTo(writePosition, this.data, offset, len)
//            for (i in 0 until len) {
//                this.data[writePosition + i] = data[i + offset]
//            }
            this@InfinityByteBuffer.readRemaining += len
            writePosition += len
            return len
        }

        fun read(data: ByteBuffer): Int {
            checkClosed()
            val len = minOf(readRemaining, data.remaining)
            if (len == 0) {
                return 0
            }
            this.data.set(readPosition, len) { buf ->
                data.write(buf)
            }
            readPosition += len
            this@InfinityByteBuffer.readRemaining -= len
            return len
        }

        val writeRemaining
            get() = data.capacity - writePosition

        val readRemaining
            get() = writePosition - readPosition
    }

    private val packages = Stack<Package>()

    private fun getReadyForWrite(): Package {
        if (packages.isEmpty) {
            val p = Package()
            packages.pushLast(p)
            return p
        }
        var last = packages.peekLast()
        if (last.writeRemaining > 0) {
            return last
        }
        last = Package()
        packages.pushLast(last)
        return last
    }

    private fun getReadyForRead(): Package? {
        while (true) {
            if (packages.isEmpty) {
                return null
            }
            val l = packages.peekFirst()
            if (l.readRemaining <= 0) {
                packages.popFirst()
                continue
            }
            return l
        }
    }

    private var closed = false

    private fun checkClosed() {
        if (closed) {
            throw StreamClosedException()
        }
    }

    override fun write(data: ByteBuffer): Int {
        checkClosed()
        if (data.remaining == 0) {
            return 0
        }
        val len = data.remaining
        while (true) {
            val p = getReadyForWrite()
            val w = p.write(data)
            if (w == 0) {
                break
            }
        }
        return len
    }

    override fun flush() {
        checkClosed()
    }

    override fun read(dest: ByteBuffer): Int {
        checkClosed()
        if (dest.remaining == 0) {
            return 0
        }
        var read = 0
        while (dest.remaining > 0) {
            val p = getReadyForRead() ?: return read
            val r = p.read(dest)
            read += r
        }
        return read
    }

    override fun close() {
        checkClosed()
        closed = true
        while (!packages.isEmpty) {
            packages.popFirst().data.close()
        }
    }
}
