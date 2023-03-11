package pw.binom.network.lite

import pw.binom.atomic.AtomicLong

class NetStatistics {
    private val _packetsSent = AtomicLong(0)
    private val _packetsReceived = AtomicLong(0)
    private val _bytesSent = AtomicLong(0)
    private val _bytesReceived = AtomicLong(0)
    private val _packetLoss = AtomicLong(0)

    public val PacketsSent
        get() = _packetsSent.getValue()
    public val PacketsReceived
        get() = _packetsReceived.getValue()
    public val BytesSent
        get() = _bytesSent.getValue()
    public val BytesReceived
        get() = _bytesReceived.getValue()
    public val PacketLoss
        get() = _packetLoss.getValue()

    public val PacketLossPercent: Long
        get() {
            val sent = PacketsSent
            val loss = PacketLoss

            return if (sent == 0L) 0 else loss * 100 / sent
        }

    public fun Reset() {
        _packetsSent.setValue(0)
        _packetsReceived.setValue(0)
        _bytesSent.setValue(0)
        _bytesReceived.setValue(0)
        _packetLoss.setValue(0)
    }

    public fun IncrementPacketsSent() {
        _packetsSent.inc()
    }

    public fun IncrementPacketsReceived() {
        _packetsReceived.inc()
    }

    public fun AddBytesSent(bytesSent: Long) {
        _bytesSent.addAndGet(bytesSent)
    }

    public fun AddBytesReceived(bytesReceived: Long) {
        _bytesReceived.addAndGet(bytesReceived)
    }

    public fun IncrementPacketLoss() {
        _packetLoss.inc()
    }

    public fun AddPacketLoss(packetLoss: Long) {
        _packetLoss.addAndGet(packetLoss)
    }

    override fun toString(): String =
        StringBuilder()
            .append("BytesReceived: ").appendLine(BytesReceived)
            .append("PacketsReceived: ").appendLine(PacketsReceived)
            .append("BytesSent: ").appendLine(BytesSent)
            .append("PacketsSent: ").appendLine(PacketsSent)
            .append("PacketLoss: ").appendLine(PacketLoss)
            .append("PacketLossPercent: ").append(PacketLossPercent)
            .toString()
}
