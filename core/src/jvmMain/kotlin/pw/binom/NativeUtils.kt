package pw.binom

actual fun <T : Any> T.doFreeze()=this
actual fun <T : Any> T.neverFreeze(): T = this