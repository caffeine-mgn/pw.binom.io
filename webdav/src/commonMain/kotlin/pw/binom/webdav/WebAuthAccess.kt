package pw.binom.webdav

import pw.binom.io.http.BasicAuth
import pw.binom.io.http.Headers
import pw.binom.io.httpClient.HttpRequest
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

interface WebAuthAccess {
    suspend fun apply(connection: HttpRequest)

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
    private val auth = BasicAuth(login = login, password = password).headerValue
    override suspend fun apply(connection: HttpRequest) {
        connection.headers[Headers.AUTHORIZATION] = auth
    }
}