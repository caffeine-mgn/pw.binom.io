package pw.binom.network

import pw.binom.Environment

actual val Short.hton: Short
    get() = this

actual val Short.ntoh: Short
    get() = this

actual val Int.hton: Int
    get() = this

actual val Int.ntoh: Int
    get() = this

actual val Long.hton: Long
    get() = this

actual val Long.ntoh: Long
    get() = this

actual val Environment.isBigEndian2: Boolean
    get() = true