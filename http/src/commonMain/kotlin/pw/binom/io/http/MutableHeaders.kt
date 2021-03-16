package pw.binom.io.http

import pw.binom.base64.Base64

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
    override var acceptEncoding: List<String>?
        get() = getSingle(Headers.ACCEPT_ENCODING)?.split(',')?.map { it.trim() }
        set(value) {
            this[Headers.ACCEPT_ENCODING] = value?.joinToString(", ")
        }
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
        get() {
            val txt = getSingle(Headers.CONTENT_LENGTH) ?: return null
            return txt?.toULongOrNull()
                ?: throw IllegalStateException("Invalid header \"${Headers.CONTENT_LENGTH}:${txt}\"")
        }
        set(value) {
            this[Headers.CONTENT_LENGTH] = value?.toString()
        }

    override var keepAlive: Boolean
        get() = getSingle(Headers.CONNECTION).equals(Headers.KEEP_ALIVE, ignoreCase = true)
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

fun <T : MutableHeaders> T.use(basicAuth: BasicAuth): T {
    this[Headers.AUTHORIZATION] = basicAuth.headerValue
    return this
}