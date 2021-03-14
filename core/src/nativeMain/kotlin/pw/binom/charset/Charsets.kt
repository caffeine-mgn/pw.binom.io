package pw.binom.charset

actual object Charsets {
    actual fun get(name: String): Charset = IconvCharset(name)

    actual val UTF8: Charset
        get() = get("utf-8")
}