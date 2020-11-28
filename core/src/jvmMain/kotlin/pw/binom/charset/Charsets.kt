package pw.binom.charset

import java.util.concurrent.ConcurrentHashMap

private val charsets = ConcurrentHashMap<String, Charset>()

actual object Charsets {
    actual fun get(name: String): Charset {
//        val n = name.toLowerCase()
//        if (n == "utf8" || n == "utf-8") {
//            return UTF8Charset
//        }
        return charsets.getOrPut(name) { JvmCharset(java.nio.charset.Charset.forName(name)) }
    }

    actual val UTF8: Charset
        get() = get("UTF-8")
}