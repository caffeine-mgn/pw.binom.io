package pw.binom.io.socket.ssl

fun ByteArray.toHex() =
    joinToString("") {
        val str = it.toUByte().toString(16)
        if (str.length == 1) {
            "0$str"
        } else {
            str
        }
    }