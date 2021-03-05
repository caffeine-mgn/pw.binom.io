package pw.binom

inline class URL internal constructor(val path: String) {
    companion object {
        fun new(
            protocol: String?,
            user: String?,
            password: String?,
            host: String,
            port: Int?,
            uri: String,
            query: String?,
            hash: String?,
        ): String {
            val sb = StringBuilder()
            if (protocol != null) {
                sb.append(protocol).append(":")
            }
            sb.append("//")
            if (user != null) {
                sb.append(user)
            }
            val auth = user != null || password != null
            if (password != null) {
                sb.append(":")
                sb.append(password)
            }
            if (user != null) {
                sb.append("@")
            }
            sb.append(host)
            if (port != null) {
                sb.append(":").append(port)
            }
            sb.append(uri)
            if (query != null) {
                sb.append("?").append(query)
            }
            if (hash != null) {
                sb.append("#").append(hash)
            }
            return sb.toString()
        }
    }

    val protocol: String?
        get() {
            val p = path.indexOf("//")
            if (p == 0) {
                return null
            }
            return path.substring(0, p - 1)
        }
    val user: String?
        get() {
            val p1 = path.indexOf('@')
            if (p1 == -1) {
                return null
            }
            val p = path.indexOf("//") + 2
            val b = path.indexOf(':', p + 2)
            if (b == -1 || b > p1) {
                return path.substring(p, p1)
            }

            return path.substring(p, b)
        }
    val password: String?
        get() {
            val p1 = path.indexOf('@')
            if (p1 == -1) {
                return null
            }
            val p = path.indexOf("//") + 2
            val b = path.indexOf(':', p + 2)
            if (b == -1 || b > p1) {
                return null
            }

            return path.substring(b + 1, p1)
        }
    val host: String
        get() {
            var p = path.indexOf('@') + 1
            if (p == 0) {
                p = path.indexOf("//") + 2
            }

            var e = path.indexOf(':', p + 1)
            if (e == -1) {
                e = path.indexOf('/', p + 1)
            }
            if (e == -1) {
                e = path.indexOf('?', p + 1)
            }

            if (e == -1) {
                e = path.indexOf('#', p + 1)
            }
            if (e == -1) {
                e = path.length
            }
            val t = path.substring(p, e)
            return path.substring(p, e)
        }
    val port: Int?
        get() {
            var s = path.indexOf("//") + 2
            s = path.indexOf(":", s)
            val c = path.indexOf('@')
            if (c != -1) {
                s = path.indexOf(":", c + 1)
                if (s == -1) {
                    return null
                }
            }
            if (s == -1) {
                return null
            }
            s++

            val hash = path.indexOf('#', s)
            val query = path.indexOf('?', s)
            val end = path.indexOf('/', s)
            var e = path.length
            e = when {
                end != -1 && end < e -> end
                query != -1 && query < e -> query
                hash != -1 && hash < e -> hash
                else -> e
            }
            return path.substring(s, e).toInt()
        }
    val uri: String
        get() {
            var s = path.indexOf("//") + 2
            s = path.indexOf('/', s)
            if (s == -1) {
                return ""
            }
            var e = path.indexOf('?')
            if (e == -1) {
                e = path.indexOf('#')
            }
            if (e == -1) {
                e = path.length
            }
            return path.substring(s, e)
        }
    val query: String?
        get() {
            val s = path.indexOf('?')
            if (s == -1) {
                return null
            }
            var e = path.indexOf('#')
            if (e == -1) {
                e = path.length
            }
            return path.substring(s, e)
        }
    val hash: String?
        get() {
            val s = path.indexOf('#')
            if (s == -1) {
                return null
            }
            return path.substring(s)
        }

    fun copy(
        protocol: String? = this.protocol,
        user: String? = this.user,
        password: String? = this.password,
        host: String = this.host,
        port: Int? = this.port,
        uri: String = this.uri,
        query: String? = this.query,
        hash: String? = this.hash,
    ) =
        URL(
            new(
                protocol = protocol,
                user = user,
                password = password,
                host = host,
                port = port,
                uri = uri,
                query = query,
                hash = hash
            )
        )

    override fun toString(): String = new(
        protocol = protocol,
        user = user,
        password = password,
        host = host,
        port = port,
        uri = uri,
        query = query,
        hash = hash
    )
}

fun String.toURLOrNull(): URL? {
    val p = indexOf("//")
    if (p == -1) {
        return null
    }
    if (indexOf("//", p + 1) != -1) {
        return null
    }
    val q = indexOf("?")
    if (q != -1 && q < p) {
        return null
    }
    if (indexOf("?", q + 1) != -1) {
        return null
    }

    val h = indexOf("#")
    if (h != -1 && q > 0 && h < q) {
        return null
    }
    if (indexOf("#", h + 1) != -1) {
        return null
    }

    return URL(this)
}

class URL2(private val path: String) {
    val protocol: String?
    val host: String
    val port: Int?
    val uri: String
    val defaultPort: Int?
        get() = protocol?.let { defaultPort(it) }

    companion object {
        fun defaultPort(protocol: String) = when (protocol) {
            "ws", "http" -> 80
            "wss", "https" -> 443
            "ftp" -> 21
            "ldap" -> 386
            else -> null
        }
    }

    init {
        val hostStart = if (path.startsWith("//")) {
            protocol = null
            2
        } else {
            val p = path.indexOf("://")

            if (p == -1)
                throw MalformedURLException("Protocol not set in URL \"$path\"")
            protocol = path.substring(0, p)
            protocol.length + 3
        }

        val uriStart = path.indexOf('/', hostStart)

        val portStart = path.indexOf(':', hostStart)
        if (portStart != -1 && (uriStart==-1 || portStart < uriStart)) {
            host = path.substring(hostStart, portStart)
            port = path.substring(portStart + 1, if (uriStart == -1) path.length else uriStart).toInt()
        } else {
            host = path.substring(hostStart, if (uriStart == -1) path.length else uriStart)
            port = null
        }
        uri = if (uriStart == -1)
            ""
        else
            path.substring(uriStart)

    }

    override fun toString(): String = path

    fun new(protocol: String? = this.protocol, host: String = this.host, port: Int? = this.port, uri: String = this.uri): URL {
        val sb = StringBuilder()
        if (protocol == null)
            sb.append("//")
        else
            sb.append(protocol).append("://")
        sb.append(host)
        if (port != null)
            sb.append(":").append(port)
        sb.append(uri)
        return URL(sb.toString())
    }

    fun newURI(uri: String): URL = new(uri = uri)

    fun appendDirectionURI(path: String) =
            if (path.isEmpty())
                this
            else
                newURI("${uri.removeSuffix("/")}/${path.removePrefix("/")}")

    fun newPort(port: Int?): URL = new(port = port)

    fun newHost(host: String): URL = new(host = host)
}