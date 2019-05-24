package pw.binom

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class TestURL {

    @Test
    fun `proto addres port`() {
        val url = URL("http://127.0.0.1:4646")
        assertEquals("http", url.protocol)
        assertEquals("", url.uri)
        assertEquals(4646, url.port)
    }

    @Test
    fun `proto addres port uri`() {
        val url = URL("http://127.0.0.1:4646/")
        assertEquals("http", url.protocol)
        assertEquals("/", url.uri)
        assertEquals(4646, url.port)
    }

    @Test
    fun `proto addres`() {
        val url = URL("http://127.0.0.1")
        assertEquals("http", url.protocol)
        assertEquals("", url.uri)
        assertNull(url.port)
    }

    @Test
    fun `proto addres uri`() {
        val url = URL("http://127.0.0.1/")
        assertEquals("http", url.protocol)
        assertEquals("/", url.uri)
        assertNull(url.port)
    }
}