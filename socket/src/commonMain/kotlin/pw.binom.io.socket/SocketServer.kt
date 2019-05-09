package pw.binom.io.socket

import pw.binom.io.Closeable

/**
 * Socket Server
 */
expect class SocketServer : Closeable {
    constructor()

    /**
     * Listen message on interface [host] and [port]
     *
     * @param host host for listen
     * @param port port for listen
     */
    fun bind(host: String = "0.0.0.0", port: Int)

    /**
     * Accepts the clients
     */
    fun accept(): Socket?
}