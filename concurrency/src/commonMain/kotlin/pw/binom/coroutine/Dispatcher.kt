package pw.binom.coroutine

import pw.binom.FreezableFuture
import pw.binom.concurrency.Reference
import kotlin.coroutines.*

@Deprecated(level = DeprecationLevel.WARNING, message = "Use kotlinx coroutines tools")
interface Dispatcher {
    companion object

    fun <T> startCoroutine(
        func: suspend () -> T
    ) = startCoroutine(context = EmptyCoroutineContext, func = func)

    fun <T> startCoroutine(
        context: CoroutineContext = EmptyCoroutineContext,
        func: suspend () -> T
    ): FreezableFuture<T>

    fun <T> startCoroutine(
        context: CoroutineContext = EmptyCoroutineContext,
        continuation: CrossThreadContinuation<T>,
        func: suspend () -> T
    )


    fun <T> resume(continuation: Reference<Continuation<T>>, result: Result<T>)
}

//@Deprecated(level = DeprecationLevel.ERROR, message = "Use kotlinx coroutines tools")
//suspend fun <T> fork(func: suspend () -> T): Future<T> =
//    suspendCoroutine {
//        val dispatcher: Dispatcher? = it.context[DispatcherCoroutineKey]?.dispatcher
//        if (dispatcher == null) {
//            it.resumeWithException(throw IllegalStateException("Current Dispatcher not found"))
//            return@suspendCoroutine
//        }
//
//        val r = dispatcher.startCoroutine(
//            context = it.context.buildCrossThreadContext(includeCurrentDispatcher = true),
//            func = func
//        )
//        it.resume(r)
//    }

//suspend fun <T> Dispatcher.start(func: suspend () -> T): T {
//    return if (Dispatcher.getCurrentDispatcher() === this) {
//        func()
//    } else {
//        val context = suspendCoroutine<CoroutineContext> {
//            it.resume(it.context.buildCrossThreadContext(includeCurrentDispatcher = false))
//        }
//        suspendManagedCoroutine {
//            this.startCoroutine(context = context, func = func.doFreeze(), continuation = it.doFreeze())
//        }
//    }
//}

//inline fun <T> Dispatcher.async(
//    context: CoroutineContext = EmptyCoroutineContext,
//    noinline func: suspend () -> T
//) = startCoroutine(context, func)

fun CoroutineContext.getDispatcherOrNull() =
    this[DispatcherCoroutineKey]?.dispatcher

fun CoroutineContext.getDispatcher() =
    getDispatcherOrNull() ?: throw IllegalStateException("Current Dispatcher not found")

suspend fun Dispatcher.Companion.getCurrentDispatcher() =
    suspendCoroutine<Dispatcher?> {
        it.resume(it.context.getDispatcherOrNull())
    }