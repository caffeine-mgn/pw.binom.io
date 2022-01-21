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
    override var acceptEncoding: List<String>
        get() = super.acceptEncoding
        set(value) {
            this[Headers.ACCEPT_ENCODING] = value.joinToString(", ")
        }
    override var contentEncoding: String?
        get() = super.contentEncoding
        set(value) {
            this[Headers.CONTENT_ENCODING] = value
        }

    override var contentType: String?
        get() = super.contentType
        set(value) {
            this[Headers.CONTENT_TYPE] = value
        }

    override var location: String?
        get() = super.location
        set(value) {
            set(Headers.LOCATION, value)
        }
    override var transferEncoding: String?
        get() = super.transferEncoding
        set(value) {
            this[Headers.TRANSFER_ENCODING] = value
        }

    fun requestBasicAuth(realm: String? = null, service: String? = null) {
        val sb = StringBuilder("Basic")
        if (realm != null) {
            sb.append(" realm=\"").append(realm).append("\"")
        }
        if (service != null) {
            if (realm != null) {
                sb.append(",")
            }
            sb.append("service=\"").append(service).append("\"")
        }
        this[Headers.WWW_AUTHENTICATE] = sb.toString()
    }

    override var contentLength: ULong?
        get() = super.contentLength
        set(value) {
            this[Headers.CONTENT_LENGTH] = value?.toString()
        }

    override var keepAlive: Boolean
        get() = super.keepAlive
        set(value) {
            if (value) {
                this[Headers.CONNECTION] = Headers.KEEP_ALIVE
            } else {
                this[Headers.CONNECTION] = Headers.CLOSE
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

fun <T : MutableHeaders> T.useBasicAuth(basicAuth: BasicAuth): T {
    this[Headers.AUTHORIZATION] = basicAuth.headerValue
    return this
}

/**
 * Applies header `Authorization: Bearer [token]`
 *
 * @receiver headers for apply bearer token
 * @param token token for add to header
 * @return this
 */
fun <T : MutableHeaders> T.useBearerAuth(token: String): T {
    this[Headers.AUTHORIZATION] = "Bearer $token"
    return this
}