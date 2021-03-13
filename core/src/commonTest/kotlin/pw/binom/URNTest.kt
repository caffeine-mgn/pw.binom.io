package pw.binom

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class URNTest {
    @Test
    fun testPath() {
//        assertTrue("/123/hello".toURN.isMatch("/{id}/hello"))
//        assertTrue("/123/hello".toURN.isMatch("/123/{id}"))
//        assertTrue("/123/hello".toURN.isMatch("/*/{id}"))
//        "/123/hello".toURN.also {
//            assertEquals("123", it.getVariable("id", "/{id}/hello"))
//            assertEquals("hello", it.getVariable("id", "/*/{id}"))
//        }

        "/my_address/my_name".toURN.getVariables("/{id}/{name}").also {
            assertEquals("my_address", it["id"])
            assertEquals("my_name", it["name"])
        }
    }
}