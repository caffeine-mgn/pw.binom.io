package pw.binom

import pw.binom.net.toPath
import kotlin.test.Test
import kotlin.test.assertEquals

class PathTest {

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

        "/a/b/v2/ooo/my_name".toPath.getVariables("{path}/v2/*/{name}")!!.also {
            assertEquals("my_name", it["name"])
            assertEquals("/a/b", it["path"])
        }

        "/a/b/v2/my_name".toPath.getVariables("{path}/v2/{name}")!!.also {
            assertEquals("my_name", it["name"])
            assertEquals("/a/b", it["path"])
        }
    }
}
