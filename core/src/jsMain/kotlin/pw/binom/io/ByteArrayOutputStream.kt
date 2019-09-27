package pw.binom.io

actual class ByteArrayOutputStream actual constructor(capacity: Int, private val capacityFactor: Float) : OutputStream {
    var data = ByteArray(capacity)

    private var closed = false
    private var writed = 0

    override fun write(data: ByteArray, offset: Int, length: Int): Int {
        if (closed)
            throw StreamClosedException()

        if (length < 0)
            throw IndexOutOfBoundsException("Length can't be less than 0")

        if (length == 0)
            return 0

        val dataForAdd = if (offset == 0 && length == data.size) {
            data
        } else
            data.asDynamic().slice(offset, offset + length).unsafeCast<ByteArray>()
        val thisData = this.data
        writed += dataForAdd.size
        js("thisData.push.apply(thisData,dataForAdd)")
        return dataForAdd.size
    }

    override fun flush() {
        if (closed)
            throw StreamClosedException()
    }

    override fun close() {
        closed = true
        data = byteArrayOf()
    }

    actual val size: Int
        get() = data.size

    actual fun toByteArray(): ByteArray {
        if (closed)
            throw StreamClosedException()
        return data.asDynamic().slice(0, writed)
    }

}