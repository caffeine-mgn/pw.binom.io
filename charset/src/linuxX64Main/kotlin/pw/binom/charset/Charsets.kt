package pw.binom.charset

import pw.binom.collections.defaultMutableMap
import pw.binom.collections.useName
import pw.binom.concurrency.SpinLock
import pw.binom.concurrency.synchronize
import kotlin.time.Duration.Companion.minutes
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
actual object Charsets {
    private val lock = SpinLock()
    private val charsets = defaultMutableMap<String, IconvCharset>().useName("Charsets") // TODO implement clean on idle

    actual fun get(name: String): Charset {
        val nameLower = name.uppercase()
        return lock.synchronize {
            val old = charsets[nameLower]
            val charset = if (old != null) {
                old.markActive()
                old
            } else {
                val new = IconvCharset(nameLower)
                charsets[nameLower] = new
                new
            }
            val it = charsets.iterator()
            while (it.hasNext()) {
                val element = it.next()
                if (element.value.lastActive.elapsedNow() > 5.minutes) {
                    element.value.close()
                    it.remove()
                }
            }
            charset
        }
    }

    actual val UTF8: Charset
        get() = get("utf-8")
}
