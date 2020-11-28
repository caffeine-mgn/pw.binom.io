package pw.binom.network

import pw.binom.ByteBuffer
import pw.binom.io.Closeable

expect class NSocket : Closeable {
    companion object {
        fun tcp(): NSocket
        fun udp(): NSocket
    }

    fun setBlocking(value: Boolean)
    fun connect(address: NetworkAddress)
    fun bind(address: NetworkAddress)
    fun accept(address: NetworkAddress.Mutable?): NSocket
    fun send(data: ByteBuffer): Int
    fun recv(data: ByteBuffer): Int

    fun send(data: ByteBuffer, address: NetworkAddress): Int
    fun recv(data: ByteBuffer, address: NetworkAddress.Mutable?): Int
}