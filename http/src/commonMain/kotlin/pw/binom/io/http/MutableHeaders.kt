package pw.binom.io.http

interface MutableHeaders : Headers {
    operator fun set(key: String, value: List<String>): MutableHeaders
    operator fun set(key: String, value: String?): MutableHeaders
    fun add(key: String, value: List<String>): MutableHeaders
    fun add(key: String, value: String): MutableHeaders
    fun add(headers: Headers): MutableHeaders
    fun remove(key: String): MutableHeaders
    fun remove(key: String, value: String): Boolean
    override operator fun get(key: String): List<String>?
    fun clear()

    override var contentEncoding: String?
        get() = getSingle(Headers.CONTENT_ENCODING)
        set(value) {
            this[Headers.CONTENT_ENCODING] = value
        }

    override var contentType: String?
        get() = getSingle(Headers.CONTENT_TYPE)
        set(value) {
            this[Headers.CONTENT_TYPE] = value
        }

    override var transferEncoding: String?
        get() = getSingle(Headers.TRANSFER_ENCODING)
        set(value) {
            this[Headers.TRANSFER_ENCODING] = value
        }

    override var keepAlive: Boolean
        get() = getSingle(Headers.CONNECTION).equals(Headers.KEEP_ALIVE, ignoreCase = true)
        set(value) {
            if (value) {
                this[Headers.CONNECTION] = Headers.KEEP_ALIVE
            } else {
                this[Headers.CONNECTION] = null
            }
        }
}

fun mutableHeadersOf(vararg headers: Pair<String, String>): MutableHeaders {
    val out = HashHeaders()
    headers.forEach {
        out.add(key = it.first, value = it.second)
    }
    return out
}

fun <T : MutableHeaders> T.use(basicAuth: BasicAuth): T {
    this[Headers.AUTHORIZATION] = basicAuth.headerValue
    return this
}