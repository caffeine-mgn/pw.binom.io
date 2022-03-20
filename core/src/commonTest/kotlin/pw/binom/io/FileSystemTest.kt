package pw.binom.io

import kotlinx.coroutines.test.runTest
import pw.binom.net.toPath
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class FileSystemTest {

    @Test
    fun mkdirsTest() = runTest {
        val fs = MemoryFileSystem()
        assertNotNull(fs.mkdirs("/pw/binom/io".toPath))
        val files = assertNotNull(fs.getDir("/pw/binom".toPath))
        assertEquals(1, files.size)
        assertEquals("io", files[0].name)
    }
}
