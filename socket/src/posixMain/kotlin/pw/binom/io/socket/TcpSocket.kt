package pw.binom.io.socket

// actual class TcpSocket private constructor(val native: RawSocket) : Socket {
//
//    actual var blocking: Boolean = false
//        set(value) {
//            setBlocking(native, value)
//            field = value
//        }
//
//    actual fun bind(path: String): Unit = TODO()
//    actual fun bind(address: NetworkAddress): Unit = TODO()
//    actual fun connect(path: String): ConnectStatus = TODO()
//    actual fun connect(address: NetworkAddress): ConnectStatus {
//        val netAddress = if (address is PosixMutableNetworkAddress)
//            address
//        else {
//            val r = PosixMutableNetworkAddress()
//            r.update(host = address.host, port = port)
//            r
//        }
//        memScoped {
//            netAddress.data.usePinned { data ->
//                platform.posix.connect(
//                    native,
//                    data.addressOf(0).getPointer(this).reinterpret(),
//                    data.get().size.convert()
//                )
//            }
//        }
//    }
//
//    actual fun send(data: ByteBuffer): Int = TODO()
//    actual fun receive(data: ByteBuffer): Int = TODO()
//    actual fun send(data: ByteBuffer, address: NetworkAddress): Int = TODO()
//    actual fun receive(data: ByteBuffer, address: MutableNetworkAddress?): Int = TODO()
// }
//
// internal fun setBlocking(native: RawSocket, value: Boolean) {
//    val flags = fcntl(native, F_GETFL, 0)
//    val newFlags = if (value) {
//        flags xor O_NONBLOCK
//    } else {
//        flags or O_NONBLOCK
//    }
//
//    if (0 != fcntl(native, F_SETFL, newFlags)) {
//        throw IOException()
//    }
// }
