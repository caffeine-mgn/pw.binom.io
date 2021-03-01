package pw.binom

class URL(private val path: String) {
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