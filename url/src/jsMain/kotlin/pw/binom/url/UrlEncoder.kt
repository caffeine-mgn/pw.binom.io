package pw.binom.url

external fun encodeURIComponent(string: String): String
external fun decodeURIComponent(string: String): String
external fun encodeURI(string: String): String
external fun decodeURI(string: String): String

actual object UrlEncoder {
    actual fun encode(input: String): String = encodeURIComponent(input)

    actual fun decode(input: String): String = decodeURIComponent(input)

    actual fun pathEncode(input: String): String = encodeURI(input)

    actual fun pathDecode(input: String): String = decodeURI(input)
}
