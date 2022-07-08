package pw.binom

import kotlin.native.concurrent.ensureNeverFrozen
import kotlin.native.concurrent.freeze
import kotlin.native.concurrent.isFrozen as isKNFrozen

actual fun <T : Any> T.doFreeze() = freeze()
actual fun <T : Any> T.neverFreeze(): T {

    ensureNeverFrozen()
    return this
}

actual val <T : Any> T.isFrozen: Boolean
    get() = isKNFrozen
