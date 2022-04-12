package pw.binom.charset

actual object Charsets {
    private val charsets = HashMap<String, IconvCharset>() // TODO implement clean on idle

    actual fun get(name: String): Charset {
        val nameLower = name.lowercase()
        return charsets.getOrPut(nameLower) { IconvCharset(name) }
    }

    actual val UTF8: Charset
        get() = get("utf-8")
}
