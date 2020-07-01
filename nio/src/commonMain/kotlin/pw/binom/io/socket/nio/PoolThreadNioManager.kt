package pw.binom.io.socket.nio

import pw.binom.io.socket.SocketSelector
import pw.binom.thread.ThreadPool

class PoolThreadNioManager(
        val threadPool: ThreadPool
) : SocketNIOManager() {

    override fun processIo(key: SocketSelector.SelectorKey) {
//        threadPool.execute {
        super.processIo(key)
//        }
    }
}