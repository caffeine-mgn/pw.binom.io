package pw.binom.io

interface InputStream : Closeable {
    fun read(data: ByteArray, offset: Int = 0, length: Int = data.size - offset): Int
}

fun InputStream.readByte(): Byte {
    val b = ByteArray(1)
    read(b, 0, 1)
    return b[0]
}

fun InputStream.readLn(): String {
    val sb = StringBuilder()
    val buf = ByteArray(1)
    while (true) {
        val r = read(buf, 0, 1)
        if (r == 0 || buf[0] == 10.toByte()) {
            return sb.toString()
        }

        if (buf[0] == 13.toByte()) {
            continue
        }
        sb.append(buf[0].toChar())
    }
}

fun InputStream.copyTo(outputStream: OutputStream, bufferSize: Int = 512) {
    val buf = ByteArray(bufferSize)
    while (true) {
        val len = read(buf, 0, buf.size)
        if (len <= 0) {
            break
        }
        outputStream.write(buf, 0, len)
    }
    outputStream.flush()
}