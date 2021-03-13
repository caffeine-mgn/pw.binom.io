package pw.binom.io

import pw.binom.*

class InfinityByteBuffer(private val packageSize: Int) : Closeable, Output, Input {
    var readRemaining = 0
        private set

    private inner class Package {
        val data = ByteBuffer.alloc(packageSize)
        private var writePosition = 0
        private var readPosition = 0

        fun write(data: ByteBuffer): Int {
            checkClosed()
            val len = minOf(writeRemaining, data.remaining)
            if (len == 0)
                return 0
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

//        fun write(data: ByteArray, offset: Int, length: Int): Int {
//            val len = minOf(writeRemaining, length)
//            if (len == 0)
//                return 0
//            this.data.write(writePosition, data, offset, len)
////            for (i in 0 until len) {
////                this.data[writePosition + i] = data[i + offset]
////            }
//            this@InfinityByteBuffer.readRemaining += len
//            writePosition += len
//            return len
//        }

        fun read(data: ByteBuffer): Int {
            checkClosed()
            val len = minOf(readRemaining, data.remaining)
            if (len == 0)
                return 0
            try {
                this.data.set(readPosition, len) { buf ->
                    data.write(buf)
                }
            } catch (e: Throwable) {
                throw e
            }
            readPosition += len
            this@InfinityByteBuffer.readRemaining -= len
            return len
        }

//        fun read(data: ByteDataBuffer, offset: Int, length: Int): Int {
//            val len = minOf(readRemaining, length)
//            if (len == 0)
//                return 0
//            try {
//                this.data.writeTo(readPosition, data, offset, len)
//            } catch (e: Throwable) {
//                throw e
//            }
//            readPosition += len
//            this@InfinityByteBuffer.readRemaining -= len
//            return len
//        }

//        fun read(data: ByteArray, offset: Int, length: Int): Int {
//            val len = minOf(readRemaining, length)
//            if (len == 0)
//                return 0
//            try {
//                this.data.read(readPosition, data, offset, len)
//            } catch (e: Throwable) {
//                throw e
//            }
//            readPosition += len
//            this@InfinityByteBuffer.readRemaining -= len
//            return len
//        }

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

//    override fun write(data: ByteDataBuffer, offset: Int, length: Int): Int {
//        if (length == 0)
//            return 0
//        var index = offset
//        var len = length
//        while (len > 0) {
//            val p = getReadyForWrite()
//            val w = p.write(data, index, len)
//            index += w
//            len -= w
//            if (w == 0) throw IllegalArgumentException()
//        }
//        return length
//    }

    override fun write(data: ByteBuffer): Int {
        checkClosed()
        if (data.remaining == 0)
            return 0
        val len = data.remaining
        while (true) {
            val p = getReadyForWrite()
            val w = p.write(data)
            if (w == 0)
                break
        }
        return len
    }

    override fun flush() {
        checkClosed()
    }

//    fun write(data: ByteArray, offset: Int = 0, length: Int = data.size - offset) {
//        if (length == 0)
//            return
//        var index = offset
//        var len = length
//        while (len > 0) {
//            val p = getReadyForWrite()
//            val w = p.write(data, index, len)
//            index += w
//            len -= w
//            if (w == 0) throw IllegalArgumentException()
//        }
//    }

//    override fun skip(length: Long): Long {
//        TODO("Not yet implemented")
//    }

    override fun read(dest: ByteBuffer): Int {
        checkClosed()
        if (dest.remaining == 0)
            return 0
        var read = 0
        while (dest.remaining > 0) {
            val p = getReadyForRead() ?: return read
            val r = p.read(dest)
            read += r
        }
        return read
    }

//    override fun read(data: ByteDataBuffer, offset: Int, length: Int): Int {
//        if (length == 0)
//            return 0
//        var index = offset
//        var len = length
//        var read = 0
//        while (len > 0) {
//            val p = getReadyForRead() ?: return read
//            val r = p.read(data, index, len)
//            read += r
//
//            index += r
//            len -= r
//        }
//        return read
//    }

//    fun read(data: ByteArray, offset: Int = 0, length: Int = data.size - offset): Int {
//        if (length == 0)
//            return 0
//        var index = offset
//        var len = length
//        var read = 0
//        while (len > 0) {
//            val p = getReadyForRead() ?: return read
//            val r = p.read(data, index, len)
//            read += r
//
//            index += r
//            len -= r
//        }
//        return read
//    }

    override fun close() {
        checkClosed()
        closed = true
        while (!packages.isEmpty) {
            packages.popFirst().data.close()
        }
    }
}