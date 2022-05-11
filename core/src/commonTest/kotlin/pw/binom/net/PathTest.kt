package pw.binom.net

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class PathTest {

    @Test
    fun parentTest() {
        assertEquals("", assertNotNull("/ololo".toPath.parent).toString())
        assertEquals("/binom", assertNotNull("/binom/ololo".toPath.parent).toString())
        assertNull("".toPath.parent)
    }

    @Test
    fun nameTest() {
        assertEquals("io", "/pw/binom/io".toPath.name)
    }

    @Test
    fun relativeTest() {
        assertEquals("/aaa", "/test/1".toPath.relative("/aaa").toString())
        assertEquals("/test/1/aaa", "/test/1".toPath.relative("aaa").toString())
        assertEquals("/test/aaa", "/test/1".toPath.relative("../aaa").toString())
        assertEquals("/test/1/aaa", "/test/1".toPath.relative("./aaa").toString())
    }

    @Test
    fun rootTest() {
        assertEquals("anton", "root/anton".toPath.removeRoot().toString())
        assertEquals("root", "root/anton".toPath.root)
    }

    @Test
    fun appendTest() {
        val b = "/test".toPath.append("video".toPath)
        println("->$b")
    }
}
