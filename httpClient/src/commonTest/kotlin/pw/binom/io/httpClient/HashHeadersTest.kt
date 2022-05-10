package pw.binom.io.httpClient

import pw.binom.io.http.HashHeaders
import pw.binom.io.http.Headers
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class HashHeadersTest {
    @Test
    fun repaceTest() {
        val v = HashHeaders()
        v[Headers.CONNECTION] = "keep-alive"
        v[Headers.CONNECTION] = Headers.WEBSOCKET
        assertEquals(1, assertNotNull(v[Headers.CONNECTION]).size)
    }
}
