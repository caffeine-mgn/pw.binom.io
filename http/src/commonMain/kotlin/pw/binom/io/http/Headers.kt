package pw.binom.io.http

import pw.binom.charset.Charset
import pw.binom.charset.Charsets

/**
 * Keywords in http headers
 */
interface Headers : Map<String, List<String>> {
    companion object {
        const val SEC_WEBSOCKET_ACCEPT = "Sec-WebSocket-Accept"
        const val SEC_WEBSOCKET_KEY = "Sec-WebSocket-Key"
        const val SEC_WEBSOCKET_VERSION = "Sec-WebSocket-Version"
        const val CONTENT_LENGTH = "Content-Length"
        const val TRANSFER_ENCODING = "Transfer-Encoding"
        const val CHUNKED = "chunked"
        const val RANGE = "Range"
        const val ORIGIN = "Origin"
        const val CONNECTION = "Connection"
        const val UPGRADE = "Upgrade"
        const val CONTENT_RANGE = "Content-Range"
        const val WEBSOCKET = "websocket"
        const val KEEP_ALIVE = "keep-alive"
        const val SET_COOKIE = "Set-Cookie"
        const val COOKIE = "Cookie"
        const val HOST = "Host"
        const val ACCEPT_ENCODING = "Accept-Encoding"
        const val ACCEPT = "Accept"
        const val CONTENT_ENCODING = "Content-Encoding"
        const val USER_AGENT = "User-Agent"
        const val CLOSE = "close"
        const val CONTENT_TYPE = "Content-Type"
        const val SERVER = "Server"
        const val LOCATION = "Location"
        const val AUTHORIZATION = "Authorization"
    }

    override operator fun get(key: String): List<String>?

    fun getContentLength(): ULong? {
        val txt = getSingle(CONTENT_LENGTH) ?: return null
        return txt?.toULongOrNull()
            ?: throw IllegalStateException("Invalid header \"${CONTENT_LENGTH}:${txt}\"")
    }

    fun getTransferEncoding() =
        getSingle(TRANSFER_ENCODING)

    fun getContentType() =
        getSingle(CONTENT_TYPE)

    fun getContentEncoding() =
        getSingle(CONTENT_ENCODING)

    fun getMimeType(): String? {
        val charset = getContentType() ?: return null
        val s = charset.indexOf(";")
        return if (s == -1) {
            charset.trim()
        } else {
            charset.substring(0, s).trim()
        }

    }

    fun getCharset(): Charset? {
        val charset = getContentType()
        return if (charset != null) {
            val s = charset.indexOf(";")
            if (s == -1) {
                null
            } else {
                val t = charset.substring(s + 1).trim().toLowerCase().removePrefix("charset=")
                Charsets.get(t)
            }
        } else {
            null
        }
    }

    fun getSingle(key: String): String? {
        val len = this[key] ?: return null
        if (len.isEmpty()) {
            return null
        }
        if (len.size > 1) {
            throw IllegalStateException("More than one head \"$key\"")
        }
        return len[0]
    }
}

/**
 * Calls [func] for each head. If [Headers] contains several values in one key [func] will called for each values
 */
inline fun Headers.forEachHeader(func: (String, String) -> Unit) {
    forEach { e ->
        e.value.forEach {
            func(e.key, it)
        }
    }
}

fun headersOf(vararg headers: Pair<String, String>): Headers {
    if (headers.isEmpty()) {
        return EmptyHeaders
    }
    return mutableHeadersOf(*headers)
}

fun emptyHeaders() = EmptyHeaders