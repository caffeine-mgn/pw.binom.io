package pw.binom.io.socket.ssl

import pw.binom.io.*
import kotlin.test.Test
import kotlin.test.assertEquals

class MessageDigestTest {

    @Test
    fun sha1() {
        val m = Sha1MessageDigest()
        m.init()
        m.update("123".encodeToByteArray())
        val result = m.finish()

        assertEquals("40BD001563085FC35165329EA1FF5C5ECBDBBEEF", result.toHex().toUpperCase())
    }

    @Test
    fun sha256() {
        val m = Sha256MessageDigest()
        m.init()
        m.update("123".encodeToByteArray())
        val result = m.finish()

        assertEquals("A665A45920422F9D417E4867EFDC4FB8A04A1F3FFF1FA07E998E86F7F7A27AE3", result.toHex().toUpperCase())
    }

    @Test
    fun sha512() {
        val m = Sha512MessageDigest()
        m.init()
        m.update("123".encodeToByteArray())
        val result = m.finish()

        assertEquals(
            "3C9909AFEC25354D551DAE21590BB26E38D53F2173B8D3DC3EEE4C047E7AB1C1EB8B85103E3BE7BA613B31BB5C9C36214DC9F14A42FD7A2FDB84856BCA5C44C2",
            result.toHex().toUpperCase()
        )
    }

    @Test
    fun md5() {
        val m = MD5MessageDigest()
        m.init()
        m.update("123".encodeToByteArray())
        val result = m.finish()

        assertEquals("202CB962AC59075B964B07152D234B70", result.toHex().toUpperCase())
    }
}