package pw.binom.network

import pw.binom.io.ByteBuffer
import pw.binom.io.Closeable

expect class RawSocket

/**
 * Native socket. Used only for native targets
 */
expect class NSocket : Closeable {
    val raw: RawSocket

    companion object {

        fun serverTcpUnixSocket(fileName: String): NSocket
        fun serverTcp(address: NetworkAddressOld): NSocket
        fun connectTcpUnixSocket(fileName: String, blocking: Boolean): NSocket
        fun connectTcp(address: NetworkAddressOld, blocking: Boolean): NSocket

        /**
         * Creates and returns udp socket
         */
        fun udp(): NSocket
    }

    /**
     * Changing blocking mode to [value]
     */
    fun setBlocking(value: Boolean)
    fun connect(address: NetworkAddressOld)
    fun bind(address: NetworkAddressOld)
    val port: Int?

    /**
     * Used for tcp server socket. Method will accept new connection. Connection of remote socket will put in to [address]
     */
    fun accept(address: NetworkAddressOld.Mutable?): NSocket?
    fun send(data: ByteBuffer): Int

    /**
     * Reads data from socket to [data]. If socket disconnected will return `-1`.
     * If return 0 in non block mode you should try read later
     */
    fun recv(data: ByteBuffer): Int

    /**
     * Usage for udp [NSocket] fro send [data] to [address]
     */
    fun send(data: ByteBuffer, address: NetworkAddressOld): Int

    /**
     * Usage for udp receiving data. Data will write to [data]. Remote address will write to [address]
     */
    fun recv(data: ByteBuffer, address: NetworkAddressOld.Mutable?): Int
}
