package pw.binom.strong

import kotlinx.cinterop.StableRef
import kotlin.native.concurrent.AtomicReference

@kotlin.native.concurrent.ThreadLocal
private val threadLocal = AtomicReference<StableRef<Strong>?>(null)

internal actual var STRONG_LOCAL: Strong?
    get() = threadLocal.value?.get()
    set(value) {
        val old = threadLocal.value?.get()
        if (value != null && old === value) {
            return
        }
        threadLocal.value?.dispose()
        threadLocal.value = if (value != null) {
            StableRef.create(value)
        } else {
            null
        }
    }
