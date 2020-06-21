package pw.binom.io.socket.nio

import pw.binom.ByteDataBuffer
import pw.binom.io.ByteBuffer
import pw.binom.pool.ObjectPool

class SingleThreadNioManager(
        packagePool: ObjectPool<ByteBuffer>,
        bufferPool: ObjectPool<ByteDataBuffer>
) : SocketNIOManager(packagePool, bufferPool) {

}