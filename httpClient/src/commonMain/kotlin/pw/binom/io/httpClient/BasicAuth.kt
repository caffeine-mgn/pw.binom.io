@file:JvmName("BasicAuthClientUtils")

package pw.binom.io.httpClient

import pw.binom.base64.Base64
import pw.binom.io.http.BasicAuth
import kotlin.jvm.JvmName

fun AsyncHttpClient.UrlConnect.use(auth:BasicAuth){
    addHeader("Authorization", "Basic ${Base64.encode("${auth.login}:${auth.password}".encodeToByteArray())}")
}