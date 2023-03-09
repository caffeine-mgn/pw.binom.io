package pw.binom

import pw.binom.io.ByteBuffer
import pw.binom.pool.ObjectPool

interface ByteBufferAllocator : ObjectPool<ByteBuffer>

open class AlwaysNewAllocator(val size: Int = DEFAULT_BUFFER_SIZE) : ByteBufferAllocator {
    override fun borrow(owner: Any?): ByteBuffer = ByteBuffer(size)
    override fun recycle(value: ByteBuffer) {
        value.close()
    }

    override fun close() {
        // Do nothing
    }
}

object DEFAULT_BYTEBUFFER_ALLOCATOR : ByteBufferAllocator {
    override fun borrow(owner: Any?): ByteBuffer {
        val obj = ByteBuffer(DEFAULT_BUFFER_SIZE)
        return obj
    }

    override fun recycle(value: ByteBuffer) {
        value.close()
    }

    override fun close() {
        // Do nothing
    }
}
