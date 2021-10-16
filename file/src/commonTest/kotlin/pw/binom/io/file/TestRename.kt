package pw.binom.io.file

import pw.binom.Environment
import pw.binom.io.use
import pw.binom.workDirectory
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

        source.openWrite().use { }

        assertFalse(dest.isFile)
        assertTrue(source.isFile)
        source.renameTo(dest)
        assertTrue(dest.isFile)
        assertFalse(source.isFile)


        dest.delete()
    }

    @Test
    fun subDirs() {
        val d1 = Environment.workDirectoryFile.relative("1")
        val d2 = Environment.workDirectoryFile.relative("2")
        d1.mkdirs()
        d2.mkdirs()
        val source = d1.relative("1")
        val dest = d2.relative("2")
        if (source.isExist)
            source.delete()

        if (dest.isExist)
            dest.delete()

        source.openWrite().use { }

        assertFalse(dest.isFile)
        assertTrue(source.isFile)
        source.renameTo(dest)
        assertTrue(dest.isFile)
        assertFalse(source.isFile)


        d1.deleteRecursive()
        d2.deleteRecursive()
    }
}