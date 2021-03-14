package pw.binom.charset

expect object Charsets {
    fun get(name: String): Charset

    val UTF8: Charset
}

class CharsetNotSupportedException(val charset:String) : RuntimeException()