package pw.binom.network

import pw.binom.concurrency.ConcurrentQueue
import pw.binom.concurrency.ThreadRef
import pw.binom.doFreeze

class CrossThreadKeyHolder(val key: Selector.Key) {
    val readyForWriteListener = ConcurrentQueue<() -> Unit>()
    private val networkThread = ThreadRef()

    fun waitReadyForWrite(func: () -> Unit) {
        if (networkThread.same) {
            func()
        } else {
            readyForWriteListener.push(func.doFreeze())
            key.addListen(Selector.OUTPUT_READY)
        }
    }
}