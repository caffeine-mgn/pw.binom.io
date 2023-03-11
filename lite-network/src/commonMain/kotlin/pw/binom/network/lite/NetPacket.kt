package pw.binom.network.lite

import kotlin.experimental.and
import kotlin.experimental.or

enum class PacketProperty(val value: Byte) {
    Unreliable(0),
    Channeled(1),
    Ack(2),
    Ping(3),
    Pong(4),
    ConnectRequest(5),
    ConnectAccept(6),
    Disconnect(7),
    UnconnectedMessage(8),
    MtuCheck(9),
    MtuOk(10),
    Broadcast(11),
    Merged(12),
    ShutdownOk(13),
    PeerNotFound(14),
    InvalidProtocol(15),
    NatMessage(16),
    Empty(17),
}

class NetPacket {
    companion object {
        private val PropertiesCount = PacketProperty.values().size
        private val HeaderSizes: IntArray

        init {

            HeaderSizes = IntArray(PropertiesCount)
            for (i in 0..HeaderSizes.size) {
                when (PacketProperty.values()[i]) {
                    PacketProperty.Channeled,
                    PacketProperty.Ack,
                    ->
                        HeaderSizes[i] = NetConstants.ChanneledHeaderSize

                    PacketProperty.Ping -> HeaderSizes[i] = NetConstants.HeaderSize + 2
                    PacketProperty.ConnectRequest -> HeaderSizes[i] = NetConnectRequestPacket.HeaderSize
                    PacketProperty.ConnectAccept -> HeaderSizes[i] = NetConnectAcceptPacket.Size
                    PacketProperty.Disconnect -> HeaderSizes[i] = NetConstants.HeaderSize + 8
                    PacketProperty.Pong -> HeaderSizes[i] = NetConstants.HeaderSize + 10
                    else -> HeaderSizes[i] = NetConstants.HeaderSize
                }
            }
        }

        public fun GetHeaderSize(property: PacketProperty) = HeaderSizes[property.value.toInt()]
    }

    // Header
    public var Property: PacketProperty
        get() = PacketProperty.values()[(RawData[0] and 0x1F).toInt()]
        set(value) {
            RawData[0] = ((RawData[0] and 0xE0.toByte()) or value.value)
        }

    public var ConnectionNumber: Byte
        get() = ((RawData[0] and 0x60.toByte()).toInt() shr 5).toByte()
        set(value) {
            RawData[0] = ((RawData[0] and 0x9F.toByte()).toInt() or (value.toInt() shl 5)).toByte()
        }

    public var Sequence: UShort
        get() = BitConverter.ToUInt16(RawData, 1)
        set(value) {
            FastBitConverter.GetBytes(RawData, 1, value)
        }

    public val IsFragmented
        get() = (RawData[0] and 0x80.toByte()) != 0.toByte()

    public fun MarkFragmented() {
        RawData[0] = RawData[0] or 0x80.toByte(); // set first bit
    }

    public var ChannelId: Byte
        get() = RawData[3]
        set(value) {
            RawData[3] = value
        }

    public var FragmentId: UShort
        get() = BitConverter.ToUInt16(RawData, 4)
        set(value) {
            FastBitConverter.GetBytes(RawData, 4, value)
        }

    public var FragmentPart: UShort
        get() = BitConverter.ToUInt16(RawData, 6)
        set(value) {
            FastBitConverter.GetBytes(RawData, 6, value)
        }

    public var FragmentsTotal: UShort
        get() = BitConverter.ToUInt16(RawData, 8)
        set(value) {
            FastBitConverter.GetBytes(RawData, 8, value)
        }

    // Data
    public var RawData: ByteArray
    public var Size = 0

    // Delivery
    public object UserData

    // Pool node
    public var Next: NetPacket? = null

    constructor(size: Int) {
        RawData = ByteArray(size)
        Size = size
    }

    constructor(property: PacketProperty, size: Int) {
        val size = size + GetHeaderSize(property)
        RawData = ByteArray(size)
        Property = property
        Size = size
    }

    public fun GetHeaderSize() = HeaderSizes[(RawData[0] and 0x1F).toInt()]

    public fun Verify(): Boolean {
        val property = RawData[0] and 0x1F
        if (property >= PropertiesCount) {
            return false
        }
        val headerSize = HeaderSizes[property.toInt()]
        val fragmented = (RawData[0] and 0x80.toByte()) != 0.toByte()
        return Size >= headerSize && (!fragmented || Size >= headerSize + NetConstants.FragmentHeaderSize)
    }
}
