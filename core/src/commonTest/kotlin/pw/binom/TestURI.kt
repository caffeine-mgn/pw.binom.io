package pw.binom

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class TestURI {

    @Test
    fun credentialTest() {
        "https://user:password@binom/".toURIOrNull()!!.apply {
            assertEquals("user", user)
            assertEquals("password", password)
        }
        "https://user@binom/".toURIOrNull()!!.apply {
            assertEquals("user", user)
            assertNull(password)
        }
        "https://@binom/".toURIOrNull()!!.apply {
            assertEquals("", user)
            assertNull(password)
        }
        "https://user:@binom/".toURIOrNull()!!.apply {
            assertEquals("user", user)
            assertEquals("", password)
        }
    }

    @Test
    fun protoTest() {
        "https://binom/".toURIOrNull()!!.apply {
            assertEquals("https", protocol)
        }
        "//binom/".toURIOrNull()!!.apply {
            assertNull(protocol)
        }
        "://binom/".toURIOrNull()!!.apply {
            assertEquals("", protocol)
        }
    }

    @Test
    fun hostTest() {
        "https://binom/".toURIOrNull()!!.apply {
            assertEquals("binom", host)
        }
        "https://binom".toURIOrNull()!!.apply {
            assertEquals("binom", host)
        }

        "//binom/".toURIOrNull()!!.apply {
            assertEquals("binom", host)
        }

        "//a:b@binom/olo".toURIOrNull()!!.apply {
            assertEquals("binom", host)
        }
    }

    @Test
    fun portTest() {
        "https://binom/olo".toURIOrNull()!!.apply {
            assertNull(port)
        }
        "https://binom:80/olo".toURIOrNull()!!.apply {
            assertEquals(80, port)
        }
        "https://binom?".toURIOrNull()!!.apply {
            assertNull(port)
        }

        "https://binom:80?q".toURIOrNull()!!.apply {
            assertEquals(80, port)
        }

        "https://binom:80#q".toURIOrNull()!!.apply {
            assertEquals(80, port)
        }
    }

    @Test
    fun uriTest() {
        "https://binom/olo".toURIOrNull()!!.apply {
            assertEquals("/olo", urn.toString())
        }

        "https://binom/olo?q".toURIOrNull()!!.apply {
            assertEquals("/olo", urn.toString())
        }
        "https://binom/olo?q#v".toURIOrNull()!!.apply {
            assertEquals("/olo", urn.toString())
        }
        "https://binom/olo#v".toURIOrNull()!!.apply {
            assertEquals("/olo", urn.toString())
        }

        "https://binom".toURIOrNull()!!.apply {
            assertEquals("", urn.toString())
        }
        "https://binom:80".toURIOrNull()!!.apply {
            assertEquals("", urn.toString())
        }
        "https://binom:80?q".toURIOrNull()!!.apply {
            assertEquals("", urn.toString())
            assertEquals("q", query)
        }
        "https://binom:80#q".toURIOrNull()!!.apply {
            assertEquals("", urn.toString())
        }
    }

    @Test
    fun common() {
        "https://user:password@binom:53/test/addr?q=1#getData".toURIOrNull()!!.apply {
            assertEquals("https", protocol)
            assertEquals("user", user)
        }

        "//user:password@binom:53/test/addr?q=1#getData".toURIOrNull()!!.apply {
            assertNull(protocol)
        }
    }

    @Test
    fun `protoAddresPort`() {
        val url = URI("http://127.0.0.1:4646")
        assertEquals("http", url.protocol)
        assertEquals("", url.urn.toString())
        assertEquals(4646, url.port)
    }

    @Test
    fun `protoAddresPortUri`() {
        val url = URI("http://127.0.0.1:4646/")
        assertEquals("http", url.protocol)
        assertEquals("/", url.urn.toString())
        assertEquals(4646, url.port)
    }

    @Test
    fun `protoAddres`() {
        val url = URI("http://127.0.0.1")
        assertEquals("http", url.protocol)
        assertEquals("", url.urn.raw)
        assertNull(url.port)
    }

    @Test
    fun `protoAddresUri`() {
        val url = URI("http://127.0.0.1/")
        assertEquals("http", url.protocol)
        assertEquals("/", url.urn.toString())
        assertNull(url.port)
    }

    @Test
    fun `withUri`() {
        var url = URI("http://127.0.0.1:4646")
        assertEquals("127.0.0.1", url.host)
        assertEquals(4646, url.port)
        url = url.copy(urn = "${url.urn}/var".toURN)

        assertEquals("/var", url.urn.toString())
    }

    @Test
    fun `withoutProto`() {
        val url = URI("//127.0.0.1:4646")
        assertNull(url.protocol)
        assertEquals("127.0.0.1", url.host)
        assertEquals(4646, url.port)
        assertEquals("", url.urn.raw)
    }

    @Test
    fun toURLOrNullTest() {
        assertNull("".toURIOrNull())
        assertNotNull("//".toURIOrNull()).apply {
            assertEquals("", host)
            assertNull(port)
            assertNull(protocol)
            assertNull(query)
            assertNull(hash)
        }
        assertNull("ht?p://".toURIOrNull())
    }
}