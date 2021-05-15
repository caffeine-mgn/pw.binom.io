package pw.binom

import pw.binom.net.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class TestURI {

    @Test
    fun appendPathTest() {
        val uri = "http://example.com/test".toURI()
        val input = uri.appendPath("user").toString()
        assertEquals("http://example.com/test/user", input)
    }

    @Test
    fun encodeTest() {
        "http://example.com/123%20456?gender=m%20w".toURI().also {
            assertEquals("/123 456", it.path.raw)
            assertEquals("m w", it.query!!.findAll("gender").single())
        }
    }

    @Test
    fun newTest() {
        val uri = URI.new(
            schema = "https",
            user = null,
            password = null,
            host = "example.com",
            port = 3301,
            path = "/123 456".toPath,
            query = Query.new(mapOf("gender" to "m w")),
            fragment = "ff nn"
        )

        assertEquals("https://example.com:3301/123%20456?gender=m%20w#ff%20nn",uri.toString())
    }

    @Test
    fun credentialTest() {
        "https://user:password@binom/".toURIOrNull!!.apply {
            assertEquals("user", user)
            assertEquals("password", password)
        }
        "https://user@binom/".toURIOrNull!!.apply {
            assertEquals("user", user)
            assertNull(password)
        }
        "https://@binom/".toURIOrNull!!.apply {
            assertEquals("", user)
            assertNull(password)
        }
        "https://user:@binom/".toURIOrNull!!.apply {
            assertEquals("user", user)
            assertEquals("", password)
        }
    }

    @Test
    fun protoTest() {
        "https://binom/".toURIOrNull!!.apply {
            assertEquals("https", schema)
        }
        "//binom/".toURIOrNull!!.apply {
            assertNull(schema)
        }
        "://binom/".toURIOrNull!!.apply {
            assertEquals("", schema)
        }
    }

    @Test
    fun hostTest() {
        "https://binom/".toURIOrNull!!.apply {
            assertEquals("binom", host)
        }
        "https://binom".toURIOrNull!!.apply {
            assertEquals("binom", host)
        }

        "//binom/".toURIOrNull!!.apply {
            assertEquals("binom", host)
        }

        "//a:b@binom/olo".toURIOrNull!!.apply {
            assertEquals("binom", host)
        }
    }

    @Test
    fun portTest() {
        "https://binom/olo".toURIOrNull!!.apply {
            assertNull(port)
        }
        "https://binom:80/olo".toURIOrNull!!.apply {
            assertEquals(80, port)
        }
        "https://binom?".toURIOrNull!!.apply {
            assertNull(port)
        }

        "https://binom:80?q".toURIOrNull!!.apply {
            assertEquals(80, port)
        }

        "https://binom:80#q".toURIOrNull!!.apply {
            assertEquals(80, port)
        }
    }

    @Test
    fun uriTest() {
        "https://binom/olo".toURIOrNull!!.apply {
            assertEquals("/olo", path.toString())
        }

        "https://binom/olo?q".toURIOrNull!!.apply {
            assertEquals("/olo", path.toString())
        }
        "https://binom/olo?q#v".toURIOrNull!!.apply {
            assertEquals("/olo", path.toString())
        }
        "https://binom/olo#v".toURIOrNull!!.apply {
            assertEquals("/olo", path.toString())
        }

        "https://binom".toURIOrNull!!.apply {
            assertEquals("", path.toString())
        }
        "https://binom:80".toURIOrNull!!.apply {
            assertEquals("", path.toString())
        }
        "https://binom:80?q".toURIOrNull!!.apply {
            assertEquals("", path.toString())
            assertEquals("q".toQuery, query)
        }
        "https://binom:80#q".toURIOrNull!!.apply {
            assertEquals("", path.toString())
        }
    }

    @Test
    fun common() {
        "https://user:password@binom:53/test/addr?q=1#getData".toURIOrNull!!.apply {
            assertEquals("https", schema)
            assertEquals("user", user)
        }

        "//user:password@binom:53/test/addr?q=1#getData".toURIOrNull!!.apply {
            assertNull(schema)
        }
    }

    @Test
    fun `protoAddresPort`() {
        val url = URI("http://127.0.0.1:4646")
        assertEquals("http", url.schema)
        assertEquals("", url.path.toString())
        assertEquals(4646, url.port)
    }

    @Test
    fun `protoAddresPortUri`() {
        val url = URI("http://127.0.0.1:4646/")
        assertEquals("http", url.schema)
        assertEquals("/", url.path.toString())
        assertEquals(4646, url.port)
    }

    @Test
    fun `protoAddres`() {
        val url = URI("http://127.0.0.1")
        assertEquals("http", url.schema)
        assertEquals("", url.path.raw)
        assertNull(url.port)
    }

    @Test
    fun `protoAddresUri`() {
        val url = URI("http://127.0.0.1/")
        assertEquals("http", url.schema)
        assertEquals("/", url.path.toString())
        assertNull(url.port)
    }

    @Test
    fun `withUri`() {
        var url = URI("http://127.0.0.1:4646")
        assertEquals("127.0.0.1", url.host)
        assertEquals(4646, url.port)
        url = url.copy(path = "${url.path}/var".toPath)

        assertEquals("/var", url.path.toString())
    }

    @Test
    fun `withoutProto`() {
        val url = URI("//127.0.0.1:4646")
        assertNull(url.schema)
        assertEquals("127.0.0.1", url.host)
        assertEquals(4646, url.port)
        assertEquals("", url.path.raw)
    }

    @Test
    fun toURLOrNullTest() {
        assertNull("".toURIOrNull)
        assertNotNull("//".toURIOrNull).apply {
            assertEquals("", host)
            assertNull(port)
            assertNull(schema)
            assertNull(query)
            assertNull(fragment)
        }
        assertNull("ht?p://".toURIOrNull)
    }
}