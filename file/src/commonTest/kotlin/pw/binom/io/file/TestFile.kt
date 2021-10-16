package pw.binom.io.file

import pw.binom.io.use
import kotlin.test.*

class TestFile {
    @Test
    fun `file Exist`() {
        val f = File("testFile")
        assertFalse(f.isExist)
        f.openWrite().use { }
        assertTrue(f.isExist)
        assertTrue(f.isFile)
        assertFalse(f.isDirectory)

        f.delete()
    }

    @Test
    fun `delete file`() {
        val f = File("testFile")
        f.openWrite().use { }
        f.delete()
        assertFalse(f.isFile)
    }

    @Test
    fun `directory`() {
        val f = File("dir")
        try {
            if (f.isExist)
                f.delete()
            assertFalse(f.isDirectory)
            f.mkdir()
            assertTrue(f.isDirectory)
            assertTrue(f.delete())
            assertFalse(f.isDirectory)
            assertFalse(f.delete())
        } finally {
            f.delete()
        }
    }

    @Test
    fun `file list`() {
        val file = File("dir1")
        if (!file.isExist)
            file.mkdir()
        try {
            file.iterator().forEach {
                assertNotEquals("..", it.name)
                assertNotEquals(".", it.name)
            }
        } finally {
            file.delete()
        }
    }

    @Test
    fun relativeTest() {
        assertEquals(
            "/home/subochev/tmp/test".replace('/', File.SEPARATOR),
            File("/home/subochev/tmp").relative("./test").path
        )
        assertEquals(
            "/home/subochev/test".replace('/', File.SEPARATOR),
            File("/home/subochev/tmp").relative("../test").path
        )
        assertEquals(
            "/home/subochev/tmp/test/m1".replace('/', File.SEPARATOR),
            File("/home/subochev/tmp").relative("test/m1").path
        )
        try {
            File("/home/subochev/tmp").relative("/test/m1")
            fail()
        } catch (e: IllegalArgumentException) {
            //NOP
        }
    }
}