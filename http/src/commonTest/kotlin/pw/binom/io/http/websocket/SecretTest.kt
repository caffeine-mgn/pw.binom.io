package pw.binom.io.http.websocket

import pw.binom.io.Sha1
import kotlin.test.Test
import kotlin.test.assertEquals

class SecretTest {
    @Test
    fun testSecret() {
        val sha1 = Sha1()
        assertEquals("0eLQeVFG0DiL/hPTDxXKLteQd2I=", HandshakeSecret.generateResponse(sha1, "M5QKAMGqeoZg4e0aoxeZHg=="))
    }
}
