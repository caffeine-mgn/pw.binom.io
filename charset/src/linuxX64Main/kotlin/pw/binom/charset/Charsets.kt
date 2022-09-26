package pw.binom.charset

import pw.binom.collections.defaultHashMap
import pw.binom.concurrency.SpinLock
import pw.binom.concurrency.synchronize

actual object Charsets {
    private val lock = SpinLock()
    private val charsets = defaultHashMap<String, IconvCharset>() // TODO implement clean on idle

    actual fun get(name: String): Charset {
        val nameLower = name.lowercase()
        return lock.synchronize { charsets.getOrPut(nameLower) { IconvCharset(name) } }
    }

    actual val UTF8: Charset
        get() = get("utf-8")
}
