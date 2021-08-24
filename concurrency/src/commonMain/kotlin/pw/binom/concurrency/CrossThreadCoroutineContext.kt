package pw.binom.concurrency

import pw.binom.coroutine.CrossThreadCoroutineContextElement
import pw.binom.coroutine.DispatcherCoroutineElement
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

fun CoroutineContext.buildCrossThreadContext(includeCurrentDispatcher: Boolean) =
    fold(EmptyCoroutineContext as CoroutineContext) { f, s ->
        if (s is DispatcherCoroutineElement && !includeCurrentDispatcher) {
            return@fold f
        }
        if (s is CrossThreadCoroutineContextElement) {
            f + s.fork()
        } else {
            f
        }
    }