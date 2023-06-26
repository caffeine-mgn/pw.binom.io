package pw.binom.io.http

import kotlin.jvm.JvmInline

@JvmInline
value class BearerAuth(val token: String) : HttpAuth {
    companion object {
        const val PREFIX = "Bearer"
    }

    override val headerValue
        get() = "$PREFIX $token"
}