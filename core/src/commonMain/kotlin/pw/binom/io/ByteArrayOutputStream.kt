package pw.binom.io

expect class ByteArrayOutputStream : OutputStream {
    constructor(capacity: Int = 512, capacityFactor: Float = 1.7f)

    fun toByteArray(): ByteArray
}