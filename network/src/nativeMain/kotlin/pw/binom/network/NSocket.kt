package pw.binom.network

import pw.binom.ByteBuffer
import pw.binom.io.Closeable
expect class RawSocket

/**
 * Native socket. Used only for native targets
 */
expect class NSocket : Closeable {
    val raw:RawSocket
    companion object {
        /**
         * Creates and return tcp socket
         */
//        fun tcp(): NSocket
        fun serverTcp(address: NetworkAddress): NSocket
        fun connectTcp(address: NetworkAddress, blocking: Boolean): NSocket

        /**
         * Creates and returns udp socket
         */
        fun udp(): NSocket
    }

    /**
     * Changing blocking mode to [value]
     */
    fun setBlocking(value: Boolean)
    fun connect(address: NetworkAddress)
    fun bind(address: NetworkAddress)
    val port: Int?

    /**
     * Used for tcp server socket. Method will accept new connection. Connection of remote socket will put in to [address]
     */
    fun accept(address: NetworkAddress.Mutable?): NSocket?
    fun send(data: ByteBuffer): Int

    /**
     * Reads data from socket to [data]. If socket disconnected will return `-1`.
     * If return 0 in non block mode you should try read later
     */
    fun recv(data: ByteBuffer): Int

    /**
     * Usage for udp [NSocket] fro send [data] to [address]
     */
    fun send(data: ByteBuffer, address: NetworkAddress): Int

    /**
     * Usage for udp receiving data. Data will write to [data]. Remote address will write to [address]
     */
    fun recv(data: ByteBuffer, address: NetworkAddress.Mutable?): Int
}
