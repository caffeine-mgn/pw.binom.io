package pw.binom

class URL(private val path: String) {
    val protocol: String
    val host: String
    val port: Int?
    val uri: String
    val defaultPort: Int

    init {
        val p = path.indexOf("://")
        if (p == -1)
            throw MalformedURLException("Protocol not set in URL \"$path\"")
        protocol = path.substring(0, p)

        defaultPort = when (protocol) {
            "ws", "http" -> 80
            "wss", "https" -> 443
            "ftp" -> 21
            else -> throw MalformedURLException("Unknown protocol \"$protocol\" in URL \"$path\"")
        }

        val uriStart = path.indexOf('/', protocol.length + 3)
        val portStart = path.indexOf(':', protocol.length + 3)
        if (portStart != -1 && (portStart < uriStart)) {
            host = path.substring(protocol.length + 3, portStart)
            port = path.substring(portStart + 1, uriStart).toInt()
        } else {
            host = path.substring(protocol.length + 3, uriStart)
            port = null
        }
        uri = if (uriStart == -1)
            ""
        else
            path.substring(uriStart)

    }

    override fun toString(): String = path


    fun newURI(uri: String): URL {
        val sb = StringBuilder(protocol)
        sb.append("://").append(host)
        if (port != null)
            sb.append(":").append(port)
        sb.append(uri)
        return URL(sb.toString())
    }

    fun newPort(port: Int?): URL {
        val sb = StringBuilder(protocol)
        sb.append("://").append(host)
        if (port != null)
            sb.append(":").append(port)
        sb.append(uri)
        return URL(sb.toString())
    }

    fun newHost(host: String): URL {
        val sb = StringBuilder(protocol)
        sb.append("://").append(host)
        if (port != null)
            sb.append(":").append(port)
        sb.append(uri)
        return URL(sb.toString())
    }
}