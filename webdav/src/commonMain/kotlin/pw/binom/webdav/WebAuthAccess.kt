package pw.binom.webdav

import pw.binom.base64.Base64
import pw.binom.encodeBytes
import pw.binom.io.httpClient.AsyncHttpClient
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

interface WebAuthAccess {
    suspend fun apply(connection: AsyncHttpClient.UrlConnect)

    class CurrentUser<T : WebAuthAccess>(val user: T) : CoroutineContext.Element {
        override val key: CoroutineContext.Key<*>
            get() = CurrentUserKey
    }

    object CurrentUserKey : CoroutineContext.Key<CurrentUser<out WebAuthAccess>>

    companion object {
        suspend fun getCurrentUser(): WebAuthAccess? =
            suspendCoroutine {
                it.resume(it.context[CurrentUserKey]?.user)
            }
    }
}

class BasicAuthorization(login: String, password: String) : WebAuthAccess {
    private val headerValue = "Basic ${Base64.encode("$login:$password".encodeBytes())}"
    override suspend fun apply(connection: AsyncHttpClient.UrlConnect) {
        connection.addHeader("Authorization", headerValue)
    }
}