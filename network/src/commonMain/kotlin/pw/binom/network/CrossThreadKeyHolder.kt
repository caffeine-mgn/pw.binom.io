package pw.binom.network

// class CrossThreadKeyHolder(val key: Selector.Key)  {
//    val readyForWriteListener = ConcurrentQueue<() -> Unit>()
//    private val networkThread = ThreadRef()
//    val isNetworkThread
//        get() = networkThread.same
//
//    init {
//        doFreeze()
//    }
//
//    fun waitReadyForWrite(func: () -> Unit) {
//        if (networkThread.same) {
//            func()
//        } else {
//            readyForWriteListener.push(func.doFreeze())
//            key.addListen(Selector.OUTPUT_READY)
//        }
//    }
//
//    fun coroutine(result: Result<Any?>, continuation: Reference<Continuation<Any?>>) {
//        waitReadyForWrite {
//            continuation.free().resumeWith(result)
//        }
//    }
// }
