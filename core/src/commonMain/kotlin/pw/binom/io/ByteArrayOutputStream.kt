package pw.binom.io

@Deprecated(message = "Use ByteArrayOutput")
expect class ByteArrayOutputStream : OutputStream {
    constructor(capacity: Int = 512, capacityFactor: Float = 1.7f)

    val size: Int
    fun toByteArray(): ByteArray
}