package pw.binom

import pw.binom.io.ByteArrayOutput
import pw.binom.io.UTF8
import pw.binom.io.use

actual fun ByteArray.asUTF8String(startIndex: Int, length: Int): String {
    val sb = StringBuilder()
    var cur = startIndex
    while (cur < cur + length) {
        val size = UTF8.utf8CharSize(this[cur])
        sb.append(UTF8.utf8toUnicode(this[cur], this, cur + 1))
        cur += size + 1
    }

    return sb.toString()
}

actual fun String.asUTF8ByteArray(): ByteArray =
        ByteArrayOutput().use {
            val data = ByteBuffer.alloc(6)
            forEach { char ->
                data.clear()
                UTF8.unicodeToUtf8(char, data)
                data.flip()
                it.write(data)
            }
            it.data.flip()
            it.data.toByteArray()
        }