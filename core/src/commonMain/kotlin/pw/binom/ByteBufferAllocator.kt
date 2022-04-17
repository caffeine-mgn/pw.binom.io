package pw.binom

import pw.binom.pool.ObjectPool

interface ByteBufferAllocator : ObjectPool<ByteBuffer>

open class AlwaysNewAllocator(val size: Int = DEFAULT_BUFFER_SIZE) : ByteBufferAllocator {
    override fun borrow(): ByteBuffer = ByteBuffer.alloc(size)
    override fun recycle(value: ByteBuffer) {
        value.close()
    }
}

object DEFAULT_BYTEBUFFER_ALLOCATOR : ByteBufferAllocator {
    override fun borrow(): ByteBuffer {
        val obj = ByteBuffer.alloc(DEFAULT_BUFFER_SIZE)
        return obj
    }

    override fun recycle(value: ByteBuffer) {
        value.close()
    }
}
