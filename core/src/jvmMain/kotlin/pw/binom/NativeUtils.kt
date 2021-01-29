package pw.binom

actual fun <T : Any> T.doFreeze()=this
actual fun <T : Any> T.neverFreeze(): T = this
actual val <T : Any> T.isFrozen: Boolean
    get() = false