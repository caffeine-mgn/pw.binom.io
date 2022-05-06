package pw.binom.io.socket.ssl

fun ByteArray.toHex() =
    joinToString("") {
        it.toUByte().toString(16).padStart(2, '0')
    }
