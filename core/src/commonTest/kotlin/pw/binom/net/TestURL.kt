package pw.binom.net

import pw.binom.url.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class TestURL {

    @Test
    fun appendPathTest() {
        val uri = "http://example.com/test".toURL()
        val input = uri.appendPath("user").toString()
        assertEquals("http://example.com/test/user", input)
    }

    @Test
    fun encodeTest() {
        "http://example.com/123%20456?gender=m%20w".toURL().also {
            assertEquals("/123 456", it.path.raw)
            assertEquals("m w", it.query!!.find("gender"))
        }
    }

    @Test
    fun newTest() {
        val uri = URL.new(
            schema = "https",
            user = null,
            password = null,
            host = "example.com",
            port = 3301,
            path = "/123 456".toPath,
            query = Query.new(mapOf("gender" to "m w")),
            fragment = "ff nn"
        )

        assertEquals("https://example.com:3301/123%20456?gender=m%20w#ff%20nn", uri.toString())
    }

    @Test
    fun credentialTest() {
        "https://user:password@binom/".toURLOrNull!!.apply {
            assertEquals("user", user)
            assertEquals("password", password)
        }
        "https://user@binom/".toURLOrNull!!.apply {
            assertEquals("user", user)
            assertNull(password)
        }
        "https://@binom/".toURLOrNull!!.apply {
            assertEquals("", user)
            assertNull(password)
        }
        "https://user:@binom/".toURLOrNull!!.apply {
            assertEquals("user", user)
            assertEquals("", password)
        }
    }

    @Test
    fun protoTest() {
        "https://binom/".toURLOrNull!!.apply {
            assertEquals("https", schema)
        }
    }

    @Test
    fun hostTest() {
        "https://binom/".toURLOrNull!!.apply {
            assertEquals("binom", host)
        }
        "https://binom".toURLOrNull!!.apply {
            assertEquals("binom", host)
        }

        "//binom/".toURLOrNull!!.apply {
            assertEquals("binom", host)
        }

        "//a:b@binom/olo".toURLOrNull!!.apply {
            assertEquals("binom", host)
        }
    }

    @Test
    fun emptyHostTest() {
        assertEquals("", "ws://:80".toURL().host)
    }

    @Test
    fun portTest() {
        "https://binom/olo".toURLOrNull!!.apply {
            assertNull(port)
        }
        "https://binom:80/olo".toURLOrNull!!.apply {
            assertEquals(80, port)
        }
        "https://binom?".toURLOrNull!!.apply {
            assertNull(port)
        }

        "https://binom:80?q".toURLOrNull!!.apply {
            assertEquals(80, port)
        }

        "https://binom:80#q".toURLOrNull!!.apply {
            assertEquals(80, port)
        }
    }

    @Test
    fun uriTest() {
        "https://binom/olo".toURLOrNull!!.apply {
            assertEquals("/olo", path.toString())
        }

        "https://binom/olo?q".toURLOrNull!!.apply {
            assertEquals("/olo", path.toString())
        }
        "https://binom/olo?q#v".toURLOrNull!!.apply {
            assertEquals("/olo", path.toString())
        }
        "https://binom/olo#v".toURLOrNull!!.apply {
            assertEquals("/olo", path.toString())
        }

        "https://binom".toURLOrNull!!.apply {
            assertEquals("", path.toString())
        }
        "https://binom:80".toURLOrNull!!.apply {
            assertEquals("", path.toString())
        }
        "https://binom:80?q".toURLOrNull!!.apply {
            assertEquals("", path.toString())
            assertEquals("q".toQuery, query)
        }
        "https://binom:80#q".toURLOrNull!!.apply {
            assertEquals("", path.toString())
        }
    }

    @Test
    fun common() {
        "https://user:password@binom:53/test/addr?q=1#getData".toURLOrNull!!.apply {
            assertEquals("https", schema)
            assertEquals("user", user)
        }
    }

    @Test
    fun `protoAddresPort`() {
        val url = "http://127.0.0.1:4646".toURL()
        assertEquals("http", url.schema)
        assertEquals("", url.path.toString())
        assertEquals(4646, url.port)
    }

    @Test
    fun schemaTest() {
        assertEquals("jdbc:mysql", "jdbc:mysql://127.0.0.1:3301/435345/e435/6457567".toURL().schema)
    }

    @Test
    fun `protoAddresPortUri`() {
        val url = "http://127.0.0.1:4646/".toURL()
        assertEquals("http", url.schema)
        assertEquals("/", url.path.toString())
        assertEquals(4646, url.port)
    }

    @Test
    fun `protoAddres`() {
        val url = "http://127.0.0.1".toURL()
        assertEquals("http", url.schema)
        assertEquals("", url.path.raw)
        assertNull(url.port)
    }

    @Test
    fun `protoAddresUri`() {
        val url = "http://127.0.0.1/".toURL()
        assertEquals("http", url.schema)
        assertEquals("/", url.path.toString())
        assertNull(url.port)
    }

    @Test
    fun `withUri`() {
        var url = "http://127.0.0.1:4646".toURL()
        assertEquals("127.0.0.1", url.host)
        assertEquals(4646, url.port)
        url = url.copy(path = "${url.path}/var".toPath)

        assertEquals("/var", url.path.toString())
    }

//    @Test
//    fun `withoutProto`() {
//        val url = URL("//127.0.0.1:4646")
//        assertNull(url.schema)
//        assertEquals("127.0.0.1", url.host)
//        assertEquals(4646, url.port)
//        assertEquals("", url.path.raw)
//    }

//    @Test
//    fun toURLOrNullTest() {
//        assertNull("".toURLOrNull)
//        assertNotNull("//".toURLOrNull).apply {
//            assertEquals("", host)
//            assertNull(port)
//            assertNull(schema)
//            assertNull(query)
//            assertNull(fragment)
//        }
//        assertNull("ht?p://".toURLOrNull)
//    }

    @Test
    fun gotoTest() {
        assertEquals("http://google.com/dev/1", "http://google.com/news/1/".toURL().goto("/dev/1".toURI()).toString())
        assertEquals("http://google.com/news/2", "http://google.com/news/1/".toURL().goto("../2".toURI()).toString())
        assertEquals("http://google.com/test", "http://google.com/news/1".toURL().goto("../test".toURI()).toString())
    }
}
