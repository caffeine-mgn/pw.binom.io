package pw.binom.cleaner

import kotlin.native.internal.createCleaner

actual class Cleaner private constructor(val native: kotlin.native.internal.Cleaner) {
    actual companion object {
        @OptIn(ExperimentalStdlibApi::class)
        actual fun <T> create(value: T, func: (T) -> Unit): Cleaner {
            val native = createCleaner(value to func) { it.second(it.first) }
            return Cleaner(native)
        }
    }
}
