package pw.binom

expect fun <T : Any> T.doFreeze(): T
expect fun <T : Any> T.neverFreeze(): T
expect val <T : Any> T.isFrozen: Boolean