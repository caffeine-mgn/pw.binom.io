package pw.binom.network

import pw.binom.async
import pw.binom.concurrency.*
import pw.binom.doFreeze
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class NetworkDispatcherHolderElement(val dispatcher: NetworkDispatcher) : CoroutineContext.Element {
    override val key: CoroutineContext.Key<NetworkDispatcherHolderElement>
        get() = NetworkDispatcherHolderElementKey
}

object NetworkDispatcherHolderElementKey : CoroutineContext.Key<NetworkDispatcherHolderElement>

class NetworkHolderElement(val keyHolder: CrossThreadKeyHolder) : CoroutineContext.Element {
    override val key: CoroutineContext.Key<NetworkHolderElement>
        get() = NetworkHolderElementKey
}

object NetworkHolderElementKey : CoroutineContext.Key<NetworkHolderElement>

suspend fun <R> execute(executor: WorkerPool? = null, func: suspend () -> R): R =
    suspendCoroutine {
        val executorPool =
            executor
                ?: it.context[ExecutorServiceHolderKey]?.executor

        if (executorPool == null) {
            it.resumeWithException(IllegalStateException("No defined default worker"))
            return@suspendCoroutine
        }
        val dispatcher = it.context[NetworkDispatcherHolderElementKey]?.dispatcher
        if (dispatcher == null) {
            it.resumeWithException(IllegalStateException("No defined default NetworkDispatcher"))
            return@suspendCoroutine
        }
        val holder = dispatcher.crossThreadWakeUpHolder.doFreeze()
        val selfCon = it.asReference()
        executorPool.submitAsync(NetworkHolderElement(holder).doFreeze()) {
            val result = runCatching { func.invoke() }
            holder.waitReadyForWrite {
                val self = selfCon.free()
                self.resumeWith(result)
            }
        }
    }

suspend fun <R> network(func: suspend () -> R): R {
    val isNetworkThread = suspendCoroutine<Boolean> { con ->
        val holder = con.context[NetworkHolderElementKey]?.keyHolder
        if (holder == null) {
            con.resumeWithException(IllegalStateException("No defined default network key holder"))
            return@suspendCoroutine
        }
        con.resume(holder.isNetworkThread)
    }
    return if (isNetworkThread) {
        func()
    } else {
        suspendCoroutine { con ->
            val holder = con.context[NetworkHolderElementKey]?.keyHolder
            if (holder == null) {
                con.resumeWithException(IllegalStateException("No defined default network key holder"))
                return@suspendCoroutine
            }
            func.doFreeze()
            val worker = con.context[WorkerHolderElementKey]?.worker
            if (worker == null) {
                con.resumeWithException(IllegalStateException("Can't find worker in Context"))
                return@suspendCoroutine
            }
            val workerContinuation = con.asReference().doFreeze()
            holder.waitReadyForWrite {
                async {
                    try {
                        val result = runCatching { func() }
                        worker.resume(result, workerContinuation)
                    } catch (e: Throwable) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

}