package pw.binom.io.http

import pw.binom.base64.Base64

data class BasicAuth(val login: String, val password: String) {
    companion object

    val base64
        get() = Base64.encode("${login}:${password}".encodeToByteArray())

    val headerValue
        get() = "Basic $base64"
}