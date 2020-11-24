package pw.binom.io.socket

import pw.binom.ByteBuffer

expect class DatagramChannel : NetworkChannel {
    companion object {
        fun bind(address: NetworkAddress): DatagramChannel
        fun open(): DatagramChannel
    }

    fun send(data: ByteBuffer, address: NetworkAddress):Int
    fun receive(data: ByteBuffer, address: MutableNetworkAddress?)

}