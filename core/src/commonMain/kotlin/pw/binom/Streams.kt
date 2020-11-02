package pw.binom

const val DEFAULT_BUFFER_SIZE = 1024 * 8

internal inline fun internal_readln(f: () -> Byte): String {
    val sb = StringBuilder()
    while (true) {
        val r = f()
        if (r == 10.toByte()) {
            return sb.toString()
        }

        if (r == 13.toByte()) {
            continue
        }
        sb.append(r.toChar())
    }
}

internal inline fun internal_write(text: String, f: (ByteArray) -> Int) = f(text.encodeBytes())
internal inline fun internal_writeln(txt: String, f: (String) -> Int) = f("$txt\r\n")