package pw.binom

import kotlin.native.internal.GC

actual object System {
    actual fun gc() {
        GC.collect()
    }
}
