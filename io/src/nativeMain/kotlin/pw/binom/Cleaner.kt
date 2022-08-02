package pw.binom

import kotlin.native.internal.createCleaner
import kotlin.native.internal.Cleaner as NativeCleaner

actual class Cleaner(val native: NativeCleaner) {
    actual companion object {
        @OptIn(ExperimentalStdlibApi::class)
        actual fun <T> create(value: T, func: (T) -> Unit): Cleaner {
            val native = createCleaner(value to func) { it.second(it.first) }
            return Cleaner(native)
        }
    }
}
