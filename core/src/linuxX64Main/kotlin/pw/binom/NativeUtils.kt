package pw.binom

import kotlin.native.concurrent.freeze
import kotlin.native.concurrent.ensureNeverFrozen

actual fun <T : Any> T.doFreeze() = freeze()
actual fun <T : Any> T.neverFreeze():T{
    ensureNeverFrozen()
    return this
}