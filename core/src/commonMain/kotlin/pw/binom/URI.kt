package pw.binom

inline class URI internal constructor(val path: String) {
    companion object {
        fun new(
            protocol: String?,
            user: String?,
            password: String?,
            host: String,
            port: Int?,
            urn: URN,
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
            sb.append(urn.raw)
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
    val urn: URN
        get() {
            var s = path.indexOf("//") + 2
            s = path.indexOf('/', s)
            if (s == -1) {
                return "".toURN
            }
            var e = path.indexOf('?')
            if (e == -1) {
                e = path.indexOf('#')
            }
            if (e == -1) {
                e = path.length
            }
            return path.substring(s, e).toURN
        }

    /**
     *
     * Retuns http request [urn]+[query]
     */
    val request: String
        get() {
            val q = query
            return if (q == null) {
                urn.raw
            } else {
                "${urn.raw}?$q"
            }
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
            return path.substring(s + 1, e)
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
        urn: URN = this.urn,
        query: String? = this.query,
        hash: String? = this.hash,
    ) =
        URI(
            new(
                protocol = protocol,
                user = user,
                password = password,
                host = host,
                port = port,
                urn = urn,
                query = query,
                hash = hash
            )
        )

    override fun toString() = path

    fun appendDirection(direction: String) =
        copy(urn = urn.appendDirection(direction = direction, separator = "/"))
}

fun String.toURIOrNull(): URI? {
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

    return URI(this)
}