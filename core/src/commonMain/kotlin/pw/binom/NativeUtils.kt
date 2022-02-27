package pw.binom

@Deprecated("Not actual for new kotlin mm")
expect fun <T : Any> T.doFreeze(): T

@Deprecated("Not actual for new kotlin mm")
expect fun <T : Any> T.neverFreeze(): T

@Deprecated("Not actual for new kotlin mm")
expect val <T : Any> T.isFrozen: Boolean
