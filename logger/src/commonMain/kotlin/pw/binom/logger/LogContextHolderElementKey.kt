package pw.binom.logger

import kotlin.coroutines.CoroutineContext

internal object LogContextHolderElementKey : CoroutineContext.Key<LogContextHolderElement>