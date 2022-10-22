package pw.binom.logger

import pw.binom.collections.defaultMutableMap
import pw.binom.collections.useName
import kotlin.coroutines.*

object StructLoggerContext {
    private suspend fun getContext() =
        suspendCoroutine<LogContextHolderElement?> {
            it.resume(it.context[LogContextHolderElementKey])
        }

    suspend fun getTags() = getContext()?.tags ?: emptyMap()

    suspend fun <T> useTags(vararg tags: Pair<String, String>, func: suspend () -> T) = useTags(tags.toMap(), func)

    suspend fun <T> useTags(tags: Map<String, String>, func: suspend () -> T) =
        suspendCoroutine<T> {
            val ctx = it.context[LogContextHolderElementKey] ?: LogContextHolderElement()
            val newTags = defaultMutableMap(ctx.tags).useName("StructLoggerContext.useTags.newTags")
            newTags.putAll(tags)
            ctx.tags = newTags

            func.startCoroutine(object : Continuation<T> {
                override val context: CoroutineContext = it.context + ctx
                override fun resumeWith(result: Result<T>) {
                    it.resumeWith(result)
                }
            })
        }
}
