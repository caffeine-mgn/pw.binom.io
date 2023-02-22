package pw.binom

import platform.common.*

actual fun threadYield() {
    internal_core_thread_yield()
}
