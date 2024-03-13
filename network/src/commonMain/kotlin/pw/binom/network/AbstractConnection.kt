package pw.binom.network

import pw.binom.io.Closeable
import pw.binom.io.socket.SelectorKey

abstract class AbstractConnection : Closeable {

    /**
     * Call by SocketNIOManager. Called in network thread when connection ready for write
     *
     * @return should return whether the connection should write more
     */
    abstract fun readyForWrite(key: SelectorKey)
    abstract suspend fun connection()
//    abstract fun connecting()

    //    abstract fun connected()
    abstract fun error()

    /**
     * Call by SocketNIOManager. Called in network thread when connection ready for read
     *
     * @return should return, should the connection read more
     */
    abstract fun readyForRead(key: SelectorKey)
}
