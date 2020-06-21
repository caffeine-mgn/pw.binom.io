package pw.binom.io.socket.nio

import pw.binom.ByteDataBuffer
import pw.binom.io.ByteBuffer
import pw.binom.io.socket.SocketSelector
import pw.binom.pool.ObjectPool
import pw.binom.thread.ThreadPool

class PoolThreadNioManager(
        packagePool: ObjectPool<ByteBuffer>,
        bufferPool: ObjectPool<ByteDataBuffer>,
        val threadPool: ThreadPool
) : SocketNIOManager(packagePool, bufferPool) {

    override fun processIo(key: SocketSelector.SelectorKey) {
//        threadPool.execute {
            super.processIo(key)
//        }
    }
}