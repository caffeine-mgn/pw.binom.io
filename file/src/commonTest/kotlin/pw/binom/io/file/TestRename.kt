package pw.binom.io.file

import pw.binom.io.use
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TestRename {

    @Test
    fun simple() {
        val source = File("1")
        val dest = File("2")
        if (source.isExist)
            source.delete()

        if (dest.isExist)
            dest.delete()

        FileOutputStream(source).use { }

        assertFalse(dest.isFile)
        assertTrue(source.isFile)
        source.renameTo(dest)
        assertTrue(dest.isFile)
        assertFalse(source.isFile)


        dest.delete()
    }

    @Test
    fun subDirs() {
        val d1 = File("1")
        val d2 = File("2")
        d1.mkdirs()
        d2.mkdirs()
        val source = File(d1, "1")
        val dest = File(d2, "2")
        if (source.isExist)
            source.delete()

        if (dest.isExist)
            dest.delete()

        FileOutputStream(source).use { }

        assertFalse(dest.isFile)
        assertTrue(source.isFile)
        source.renameTo(dest)
        assertTrue(dest.isFile)
        assertFalse(source.isFile)


        d1.deleteRecursive()
        d2.deleteRecursive()
    }
}