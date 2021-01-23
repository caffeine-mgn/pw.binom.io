package pw.binom.network

import pw.binom.async
import pw.binom.concurrency.*
import pw.binom.doFreeze
import kotlin.coroutines.CoroutineContext
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

suspend fun <R> execute(executor: WorkerPool? = null, func: suspend () -> R) {
    suspendCoroutine<R> {
        val executorPool =
            executor
                ?: it.context[ExecutorPoolHolderKey]?.executor
                ?: throw IllegalStateException("No defined default worker")
        val dispatcher = it.context[NetworkDispatcherHolderElementKey]?.dispatcher
            ?: throw IllegalStateException("No defined default NetworkDispatcher")
        val holder = dispatcher.crossThreadWakeUpHolder.doFreeze()
        val selfCon = it.asReference()
        executorPool.submitAsync(NetworkHolderElement(holder)) {
            val result = runCatching { func.invoke() }
            holder.waitReadyForWrite {
                val self = selfCon.free()
                self.resumeWith(result)
            }
        }
    }
}

suspend fun <R> network(func: suspend () -> R) =
    suspendCoroutine<R> { con ->
        val holder = con.context[NetworkHolderElementKey]?.keyHolder
            ?: throw IllegalStateException("No defined default network key holder")
        func.doFreeze()
        val worker = con.context[WorkerHolderElementKey]?.worker
            ?: throw IllegalStateException("Can't find worker in Context")
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