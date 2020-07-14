package pw.binom.webdav

import pw.binom.asUTF8ByteArray
import pw.binom.base64.Base64
import pw.binom.io.httpClient.AsyncHttpClient

interface WebAuthAccess {
    suspend fun apply(connection: AsyncHttpClient.UrlConnect)
}

class BasicAuthorization(login: String, password: String) : WebAuthAccess {
    private val headerValue = "Basic ${Base64.encode("$login:$password".asUTF8ByteArray())}"
    override suspend fun apply(connection: AsyncHttpClient.UrlConnect) {
        connection.addHeader("Authorization", headerValue)
    }
}