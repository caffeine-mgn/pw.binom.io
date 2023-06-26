package pw.binom.io.http

sealed interface HttpAuth {
    val headerValue: String
}