package pw.binom.url

expect object UrlEncoder {
    fun encode(input: String): String
    fun decode(input: String): String
    fun pathEncode(input: String): String
    fun pathDecode(input: String): String
}
