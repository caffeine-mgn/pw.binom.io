package pw.binom.io

open class ConnectException(message: String? = null) : SocketException(message = message) {
    constructor(host: String, port: Int) : this("$host:$port")
}