package pw.binom.network.lite

/**
 * Sending method type
 */
enum class DeliveryMethod(val value: Byte) {
    /**
     * Unreliable. Packets can be dropped, can be duplicated, can arrive without order.
     */
    Unreliable(4),

    /**
     * Reliable. Packets won't be dropped, won't be duplicated, can arrive without order.
     */
    ReliableUnordered(0),

    /**
     * Unreliable. Packets can be dropped, won't be duplicated, will arrive in order.
     */
    Sequenced(1),

    /**
     * Reliable and ordered. Packets won't be dropped, won't be duplicated, will arrive in order.
     */
    ReliableOrdered(2),

    /**
     * Reliable only last packet. Packets can be dropped (except the last one), won't be duplicated, will arrive in order.
     * Cannot be fragmented
     */
    ReliableSequenced(3),
}

/**
 * Network constants. Can be tuned from sources for your purposes.
 */
object NetConstants {

    // can be tuned
    const val DefaultWindowSize = 64
    const val SocketBufferSize = 1024 * 1024; // 1mb
    const val SocketTTL = 255

    const val HeaderSize = 1
    const val ChanneledHeaderSize = 4
    const val FragmentHeaderSize = 6
    const val FragmentedHeaderTotalSize = ChanneledHeaderSize + FragmentHeaderSize
    val MaxSequence = 32768.toUShort()
    val HalfMaxSequence = (MaxSequence / 2.toUShort()).toUShort()

    // protocol
    internal const val ProtocolId = 13
    internal const val MaxUdpHeaderSize = 68
    internal const val ChannelTypeCount = 4

    internal val PossibleMtu =
        listOf(
            576 - MaxUdpHeaderSize, // minimal (RFC 1191)
            1024, // most games standard
            1232 - MaxUdpHeaderSize,
            1460 - MaxUdpHeaderSize, // google cloud
            1472 - MaxUdpHeaderSize, // VPN
            1492 - MaxUdpHeaderSize, // Ethernet with LLC and SNAP, PPPoE (RFC 1042)
            1500 - MaxUdpHeaderSize, // Ethernet II (RFC 1191)
        )

    // Max possible single packet size
    val MaxPacketSize = PossibleMtu[PossibleMtu.size - 1]
    val MaxUnreliableDataSize = MaxPacketSize - HeaderSize

    // peer specific
    const val MaxConnectionNumber = 4.toByte()
}
