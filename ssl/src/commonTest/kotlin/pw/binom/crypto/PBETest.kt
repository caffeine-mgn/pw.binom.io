package pw.binom.crypto

import pw.binom.io.socket.ssl.toHex
import kotlin.test.Test

class PBETest {
    @Test
    fun test() {
        val b = PBE.scrypt(
            password = "123",
            salt = ByteArray(10),
            keyLength = 103,
        )
        println("->${b.toHex()}")
    }
}

// fun ByteArray.toHex() = map { it.toUByte().toString(16).padStart(2, '0') }.joinToString("")
