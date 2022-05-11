package pw.binom.net

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class TestUri {
    @Test
    fun schemaTest() {
        assertEquals("http", URI("http://google.com").schema)
        assertEquals("", URI("//google.com").schema)
        assertNull(URI("google.com").schema)
    }

    @Test
    fun testPath() {
        val PATH = "google.com/test"
        assertEquals(PATH, URI("http://$PATH").path.toString())
        assertEquals(PATH, URI("http://$PATH?t=1").path.toString())
        assertEquals(PATH, URI("http://$PATH?t=1#v=2").path.toString())
        assertEquals(PATH, URI("http://$PATH#v=2").path.toString())
        assertEquals(PATH, URI(PATH).path.toString())
        assertEquals(PATH, URI("$PATH?t=1#v=2").path.toString())
    }

    @Test
    fun testAddPath() {
        assertEquals("/api/videos", "/api".toURI().appendPath("videos".toPath).toString())
    }
}
