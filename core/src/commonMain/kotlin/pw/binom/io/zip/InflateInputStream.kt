package pw.binom.io.zip

import pw.binom.io.InputStream

class InflateInputStream(val stream: InputStream, bufferSize: Int = 512, wrap: Boolean = false) : InputStream {
    private val buf = ByteArray(bufferSize)
    private val inflater = Inflater(wrap)

    private var cursor = Cursor()
    private var first = true

    protected fun full() {
        if (!first && cursor.availIn > 0)
            return

        cursor.inputOffset = 0
        cursor.inputLength = stream.read(buf, 0, buf.size)
        cursor.inputLength = maxOf(0, cursor.inputLength)
        first = false
    }

    override fun read(data: ByteArray, offset: Int, length: Int): Int {
        cursor.outputLength = length
        cursor.outputOffset = offset
        while (true) {
            full()
            if (cursor.availIn == 0 || cursor.availOut == 0)
                break
            println("Read data: $cursor")
            inflater.inflate(cursor, buf, data)
        }
        return length - cursor.availOut
    }

    override fun close() {
        inflater.close()
    }

}