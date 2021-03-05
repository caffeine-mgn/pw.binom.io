package pw.binom

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class TestURL {

    @Test
    fun credentialTest() {
        "https://user:password@binom/".toURLOrNull()!!.apply {
            assertEquals("user", user)
            assertEquals("password", password)
        }
        "https://user@binom/".toURLOrNull()!!.apply {
            assertEquals("user", user)
            assertNull(password)
        }
        "https://@binom/".toURLOrNull()!!.apply {
            assertEquals("", user)
            assertNull(password)
        }
        "https://user:@binom/".toURLOrNull()!!.apply {
            assertEquals("user", user)
            assertEquals("", password)
        }
    }

    @Test
    fun protoTest() {
        "https://binom/".toURLOrNull()!!.apply {
            assertEquals("https", protocol)
        }
        "//binom/".toURLOrNull()!!.apply {
            assertNull(protocol)
        }
        "://binom/".toURLOrNull()!!.apply {
            assertEquals("", protocol)
        }
    }

    @Test
    fun hostTest() {
        "https://binom/".toURLOrNull()!!.apply {
            assertEquals("binom", host)
        }
        "https://binom".toURLOrNull()!!.apply {
            assertEquals("binom", host)
        }

        "//binom/".toURLOrNull()!!.apply {
            assertEquals("binom", host)
        }

        "//a:b@binom/olo".toURLOrNull()!!.apply {
            assertEquals("binom", host)
        }
    }

    @Test
    fun portTest() {
        "https://binom/olo".toURLOrNull()!!.apply {
            assertNull(port)
        }
        "https://binom:80/olo".toURLOrNull()!!.apply {
            assertEquals(80, port)
        }
        "https://binom?".toURLOrNull()!!.apply {
            assertNull(port)
        }

        "https://binom:80?q".toURLOrNull()!!.apply {
            assertEquals(80, port)
        }

        "https://binom:80#q".toURLOrNull()!!.apply {
            assertEquals(80, port)
        }
    }

    @Test
    fun uriTest() {
        "https://binom/olo".toURLOrNull()!!.apply {
            assertEquals("/olo", uri)
        }

        "https://binom/olo?q".toURLOrNull()!!.apply {
            assertEquals("/olo", uri)
        }
        "https://binom/olo?q#v".toURLOrNull()!!.apply {
            assertEquals("/olo", uri)
        }
        "https://binom/olo#v".toURLOrNull()!!.apply {
            assertEquals("/olo", uri)
        }

        "https://binom".toURLOrNull()!!.apply {
            assertEquals("", uri)
        }
        "https://binom:80".toURLOrNull()!!.apply {
            assertEquals("", uri)
        }
        "https://binom:80?q".toURLOrNull()!!.apply {
            assertEquals("", uri)
        }
        "https://binom:80#q".toURLOrNull()!!.apply {
            assertEquals("", uri)
        }
    }

    @Test
    fun common() {
        "https://user:password@binom:53/test/addr?q=1#getData".toURLOrNull()!!.apply {
            assertEquals("https", protocol)
            assertEquals("user", user)
        }

        "//user:password@binom:53/test/addr?q=1#getData".toURLOrNull()!!.apply {
            assertNull(protocol)
        }
    }

    @Test
    fun `protoAddresPort`() {
        val url = URL("http://127.0.0.1:4646")
        assertEquals("http", url.protocol)
        assertEquals("", url.uri)
        assertEquals(4646, url.port)
    }

    @Test
    fun `protoAddresPortUri`() {
        val url = URL("http://127.0.0.1:4646/")
        assertEquals("http", url.protocol)
        assertEquals("/", url.uri)
        assertEquals(4646, url.port)
    }

    @Test
    fun `protoAddres`() {
        val url = URL("http://127.0.0.1")
        assertEquals("http", url.protocol)
        assertEquals("", url.uri)
        assertNull(url.port)
    }

    @Test
    fun `protoAddresUri`() {
        val url = URL("http://127.0.0.1/")
        assertEquals("http", url.protocol)
        assertEquals("/", url.uri)
        assertNull(url.port)
    }

    @Test
    fun `withUri`() {
        var url = URL("http://127.0.0.1:4646")
        assertEquals("127.0.0.1", url.host)
        assertEquals(4646, url.port)
        url = url.copy(uri = "${url.uri}/var")

        assertEquals("/var", url.uri)
    }

    @Test
    fun `withoutProto`() {
        val url = URL("//127.0.0.1:4646")
        assertNull(url.protocol)
        assertEquals("127.0.0.1", url.host)
        assertEquals(4646, url.port)
        assertEquals("", url.uri)
    }

    @Test
    fun toURLOrNullTest() {
        assertNull("".toURLOrNull())
        assertNotNull("//".toURLOrNull()).apply {
            assertEquals("", host)
            assertNull(port)
            assertNull(protocol)
            assertNull(query)
            assertNull(hash)
        }
        assertNull("ht?p://".toURLOrNull())
    }
}