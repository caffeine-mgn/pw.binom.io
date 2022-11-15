package pw.binom.network

import pw.binom.io.Closeable

abstract class AbstractConnection : Closeable {

    /**
     * Call by SocketNIOManager. Called in network thread when connection ready for write
     *
     * @return should return whether the connection should write more
     */
    abstract fun readyForWrite(key: Selector.Key)
    abstract fun connecting()
    abstract fun connected()
    abstract fun error()
    abstract fun cancelSelector()

    /**
     * Call by SocketNIOManager. Called in network thread when connection ready for read
     *
     * @return should return, should the connection read more
     */
    abstract fun readyForRead(key: Selector.Key)
}
