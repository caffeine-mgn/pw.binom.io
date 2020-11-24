package pw.binom.io.socket

import pw.binom.ByteBuffer
import java.net.Inet4Address
import java.net.InetSocketAddress
import java.nio.channels.SelectableChannel
import java.nio.channels.DatagramChannel as JDatagramChannel

actual class DatagramChannel(val native: JDatagramChannel) : NetworkChannel {
    actual companion object {
        actual fun bind(address: NetworkAddress): DatagramChannel {
            val channel = JDatagramChannel.open()
            channel.bind(InetSocketAddress(address.host, address.port))
            channel.configureBlocking(false)
            return DatagramChannel(channel)
        }

        actual fun open(): DatagramChannel {
            val channel = JDatagramChannel.open()
            channel.configureBlocking(false)
            return DatagramChannel(channel)
        }
    }

    actual fun send(data: ByteBuffer, address: NetworkAddress): Int{
        val addr = if (address is MutableNetworkAddress){
            address.native
        } else {
            InetSocketAddress(address.host, address.port)
        }
        return native.send(data.native, addr)
    }

    actual fun receive(data: ByteBuffer, address: MutableNetworkAddress?) {
        val addr = native.receive(data.native)
        if (address is MutableNetworkAddress && addr is InetSocketAddress) {
            address.overrideNative(addr)
        } else {
            if (address != null) {
                when (addr) {
                    is InetSocketAddress -> {
                        address.reset(
                            host = addr.address.hostAddress,
                            port = addr.port
                        )
                    }
                    else -> throw IllegalStateException("Unknown addres type: [${addr::class.java}]")
                }
            }
        }
    }

    override val selectableChannel: SelectableChannel
        get() = native

    override val accepteble: Boolean
        get() = false

    override fun close() {
        native.close()
    }

}