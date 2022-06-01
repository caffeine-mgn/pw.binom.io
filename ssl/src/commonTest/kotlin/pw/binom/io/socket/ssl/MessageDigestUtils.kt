package pw.binom.io.socket.ssl

fun ByteArray.toHex() = joinToString("") {
    it.toUByte().toString(16).padStart(2, '0')
}

fun String.hexToByteArray() = ByteArray(length / 2) {
    val start = it * 2
    substring(start, start + 2).toUByte(16).toByte()
}
