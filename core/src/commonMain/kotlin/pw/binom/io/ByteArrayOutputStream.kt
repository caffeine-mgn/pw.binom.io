package pw.binom.io

expect class ByteArrayOutputStream : OutputStream {
    constructor(capacity: Int = 512, capacityFactor: Float = 1.7f)

    val size: Int
    fun toByteArray(): ByteArray
}