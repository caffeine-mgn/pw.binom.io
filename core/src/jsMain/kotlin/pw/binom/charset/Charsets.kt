package pw.binom.charset

actual object Charsets {
    actual fun get(name: String): Charset {
        val c = name.lowercase()
        if (c == "utf8" || c == "utf-8") {
            return UTF8
        }
        throw RuntimeException("Charset \"$name\" not supported")
    }

    actual val UTF8: Charset = UTF8Charset
}