package pw.binom.io.httpClient

internal actual fun internalCreateHttpClient(): HttpClient = HttpClient.create()

fun HttpClient.Companion.create(): JsBaseHttpClient = JsBaseHttpClient()
