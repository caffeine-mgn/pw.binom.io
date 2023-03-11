package pw.binom.network.lite

import pw.binom.io.Buffer

internal class NetConnectRequestPacket {
    companion object {
        const val HeaderSize = 18

        public fun GetProtocolId(packet: NetPacket): Int = BitConverter.ToInt32(packet.RawData, 1)

        public fun FromData(packet: NetPacket): NetConnectRequestPacket? {
            if (packet.ConnectionNumber >= NetConstants.MaxConnectionNumber) {
                return null
            }

            // Getting connection time for peer
            val connectionTime = BitConverter.ToInt64(packet.RawData, 5)

            // Get peer id
            val peerId = BitConverter.ToInt32(packet.RawData, 13)

            // Get target address
            val addrSize = packet.RawData[HeaderSize - 1].toInt()
            if (addrSize != 16 && addrSize != 28) {
                return null
            }
            val addressBytes = ByteArray(addrSize)
            Buffer.BlockCopy(packet.RawData, HeaderSize, addressBytes, 0, addrSize)

            // Read data and create request
            val reader = NetDataReader(null, 0, 0)
            if (packet.Size > HeaderSize + addrSize) {
                reader.SetSource(packet.RawData, HeaderSize + addrSize, packet.Size)
            }

            return NetConnectRequestPacket(connectionTime, packet.ConnectionNumber, peerId, addressBytes, reader)
        }

        public fun Make(
            connectData: NetDataWriter,
            addressBytes: SocketAddress,
            connectTime: Long,
            localId: Int,
        ): NetPacket {
            // Make initial packet
            var packet = NetPacket(PacketProperty.ConnectRequest, connectData.Length + addressBytes.Size)

            // Add data
            FastBitConverter.GetBytes(packet.RawData, 1, NetConstants.ProtocolId)
            FastBitConverter.GetBytes(packet.RawData, 5, connectTime)
            FastBitConverter.GetBytes(packet.RawData, 13, localId)
            packet.RawData[HeaderSize - 1] = addressBytes.Size.toByte()
            for (i in 0..addressBytes.Size) {
                packet.RawData[HeaderSize + i] = addressBytes[i]
            }
            Buffer.BlockCopy(connectData.Data, 0, packet.RawData, HeaderSize + addressBytes.Size, connectData.Length)
            return packet
        }
    }

    public val ConnectionTime: Long
    public var ConnectionNumber = 0.toByte()
    public val TargetAddress: ByteArray
    public val Data: NetDataReader
    public val PeerId: Int

    private constructor(
        connectionTime: Long,
        connectionNumber: Byte,
        localId: Int,
        targetAddress: ByteArray,
        data: NetDataReader,
    ) {
        ConnectionTime = connectionTime
        ConnectionNumber = connectionNumber
        TargetAddress = targetAddress
        Data = data
        PeerId = localId
    }
}

internal class NetConnectAcceptPacket {
    companion object {
        public const val Size = 15
        public fun FromData(packet: NetPacket): NetConnectAcceptPacket? {
            if (packet.Size != Size) {
                return null
            }

            val connectionId = BitConverter.ToInt64(packet.RawData, 1)

            // check connect num
            val connectionNumber = packet.RawData[9]
            if (connectionNumber >= NetConstants.MaxConnectionNumber) {
                return null
            }

            // check reused flag
            val isReused = packet.RawData[10]
            if (isReused > 1) {
                return null
            }

            // get remote peer id
            val peerId = BitConverter.ToInt32(packet.RawData, 11)
            if (peerId < 0) {
                return null
            }

            return NetConnectAcceptPacket(connectionId, connectionNumber, peerId, isReused == 1.toByte())
        }

        public fun Make(connectTime: Long, connectNum: Byte, localPeerId: Int): NetPacket {
            val packet = NetPacket(PacketProperty.ConnectAccept, 0)
            FastBitConverter.GetBytes(packet.RawData, 1, connectTime)
            packet.RawData[9] = connectNum
            FastBitConverter.GetBytes(packet.RawData, 11, localPeerId)
            return packet
        }

        public fun MakeNetworkChanged(peer: NetPeer): NetPacket {
            var packet = NetPacket(PacketProperty.PeerNotFound, Size - 1)
            FastBitConverter.GetBytes(packet.RawData, 1, peer.ConnectTime)
            packet.RawData[9] = peer.ConnectionNum
            packet.RawData[10] = 1
            FastBitConverter.GetBytes(packet.RawData, 11, peer.RemoteId)
            return packet
        }
    }

    public val ConnectionTime: Long
    public val ConnectionNumber: Byte
    public val PeerId: Int
    public val PeerNetworkChanged: Boolean

    private constructor(connectionTime: Long, connectionNumber: Byte, peerId: Int, peerNetworkChanged: Boolean) {
        ConnectionTime = connectionTime
        ConnectionNumber = connectionNumber
        PeerId = peerId
        PeerNetworkChanged = peerNetworkChanged
    }
}
