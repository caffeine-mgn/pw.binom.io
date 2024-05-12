package pw.binom.io.socket

import java.nio.channels.DatagramChannel

class JvmUdpSocket(native: DatagramChannel) : AbstractJvmUdpSocket(native)
