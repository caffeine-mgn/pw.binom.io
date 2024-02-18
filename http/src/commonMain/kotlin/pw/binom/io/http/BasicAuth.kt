package pw.binom.io.http

import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@OptIn(ExperimentalEncodingApi::class)
data class BasicAuth(val login: String, val password: String) : HttpAuth {
    companion object {
        const val PREFIX = "Basic"
    }

    val base64
        get() = Base64.encode("$login:$password".encodeToByteArray())

    override val headerValue
        get() = "$PREFIX $base64"
}
