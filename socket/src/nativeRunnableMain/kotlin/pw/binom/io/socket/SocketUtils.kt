package pw.binom.io.socket

import pw.binom.io.ByteBuffer

expect fun bindUnixSocket(native: RawSocket, fileName: String): BindStatus
expect fun unbind(native: RawSocket)
expect fun setBlocking(native: RawSocket, value: Boolean)
expect fun allowIpv4(native: RawSocket)
expect fun internalAccess(native: RawSocket, address: MutableNetworkAddress?): RawSocket?

expect fun internalReceive(native: RawSocket, data: ByteBuffer, address: MutableNetworkAddress?): Int
