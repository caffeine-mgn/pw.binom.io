package pw.binom.charset

import java.util.concurrent.ConcurrentHashMap
import java.nio.charset.Charset as JvmCharset

private val charsets = ConcurrentHashMap<String, Charset>()

actual object Charsets {
    actual fun get(name: String): Charset =
        charsets.getOrPut(name) { JvmCharset(JvmCharset.forName(name)) }

    actual val UTF8: Charset
        get() = get("UTF-8")
}
