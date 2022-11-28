package pw.binom.url

import kotlin.test.*

class PathTest {
    @Test
    fun pathAppendTest() {
        assertEquals("http://google.com/search/test", ("http://google.com/search".toURL() + "/test".toPath).toString())
        assertEquals("http://google.com/test", ("http://google.com".toURL() + "/test".toPath).toString())
        assertEquals("http://google.com/test", ("http://google.com/".toURL() + "/test".toPath).toString())
    }

    @Test
    fun testAppend() {
        assertEquals("/test/user", "/test".toPath.append("user").toString())
    }

    @Test
    fun testPath() {
        "/my_address/my_name".toPath.getVariables("/{id}/{name}")!!.also {
            assertEquals("my_address", it["id"])
            assertEquals("my_name", it["name"])
        }
    }

    @Test
    fun severalDirectionInStar() {
        "/a/b/v2/my_name".toPath.getVariables("*/v2/{name}")!!.also {
            assertEquals("my_name", it["name"])
        }
    }

    @Test
    fun namedParts() {
        "/a/b/v2/ooo/my_name".toPath.getVariables("/a/{path}/v2/*/{name}")!!.also {
            assertEquals("my_name", it["name"])
            assertEquals("b", it["path"])
        }

        "/a/b/v2/my_name".toPath.getVariables("/a/{path}/v2/{name}")!!.also {
            assertEquals("my_name", it["name"])
            assertEquals("b", it["path"])
        }

        assertNull("/11/22/33".toPath.getVariables("/{a}/{b}"))
        val bb =
            "/api/catalog/0x0cfe3017334bd65c70978153ae5e1cf7777cba92/0ae676c8-c1a3-4023-9ae9-90ef96d693af".toPath.getVariables(
                "/api/catalog/{address}/"
            )
        assertNull(bb)

        val nn =
            "/api/catalog/0x0cfe3017334bd65c70978153ae5e1cf7777cba92/0ae676c8-c1a3-4023-9ae9-90ef96d693af/7423d4f9-f183-4670-afc4-b303ee1cb200".toPath.isMatch(
                "/api/catalog/{address}/"
            )
        assertFalse(nn)
    }

    @Test
    fun matchTest() {
//        val path =
//            "/123/456/".toPath
//        val mask = "/{address}/".toPathMask()
//        assertFalse(path.isMatch(mask))

        assertTrue("/ab/cd/ef".toPath.isMatch("*/ef"))
    }
}
