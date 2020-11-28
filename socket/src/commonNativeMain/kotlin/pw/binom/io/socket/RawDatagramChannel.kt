package pw.binom.io.socket

import pw.binom.ByteBuffer

actual class DatagramChannel(val native: NativeSocketHolder) : NetworkChannel {
    actual companion object {
        actual fun bind(address: NetworkAddress): DatagramChannel {
            val native = createDatagramSocket()
            bindDatagram(native, address)
            setBlocking(native, false)
            return DatagramChannel(native)
        }

        actual fun open(): DatagramChannel {
            val native = createDatagramSocket()
            setBlocking(native, false)
            return DatagramChannel(native)
        }
    }

    actual fun send(data: ByteBuffer, address: NetworkAddress): Int {
        writeDatagram(native, data, address)
        return 0
    }

    actual fun receive(data: ByteBuffer, address: MutableNetworkAddress?) {
        readDatagram(native, data, address)
    }

    override val nsocket: NativeSocketHolder
        get() = native
    override val type: Int
        get() = TODO("Not yet implemented")

    override fun close(){
        closeSocket(native)
    }

}