package pw.binom.webdav.client

import pw.binom.charset.Charsets
import pw.binom.io.*
import pw.binom.net.toPath
import pw.binom.nextUuid
import kotlin.random.Random
import kotlin.test.*

abstract class AbstractWebDavClientTest {
    protected abstract fun clientWithUser(func: suspend (WebDavClient) -> Unit)

    @Test
    fun putGetDeleteFileTest() {
        clientWithUser { client ->
            var dir = client.getDir("/".toPath)!!.toList()
            assertTrue(dir.isEmpty(), message = "Actual value: $dir")
            val tmpContent = Random.nextUuid().toString()
            client.new("/new.txt".toPath).bufferedAsciiWriter().use {
                it.append(tmpContent)
            }
            dir = client.getDir("/".toPath)!!.toList()
            assertEquals(1, dir.size)
            assertEquals("new.txt", dir[0].name)
            val vv = client.get("/new.txt".toPath)
            assertEquals("/new.txt", vv!!.path.toString())
            println("->$vv")
            val txt = dir[0].read()!!.bufferedReader().use { it.readText() }
            assertEquals(tmpContent, txt)
            dir[0].delete()
            dir = client.getDir("/".toPath)!!.toList()
            assertTrue(dir.isEmpty(), message = "Actual value: $dir")
        }
    }

    @Test
    fun copyTest() {
        clientWithUser { client ->
            val tmpContent = Random.nextUuid().toString()
            client.new("/new.txt".toPath).bufferedAsciiWriter().use {
                it.append(tmpContent)
            }
            val newTxt = client.get("/new.txt".toPath)!!
            newTxt.copy("/new2.txt".toPath)
            println("Coping done!")
            val new2Txt = client.get("/new2.txt".toPath)
            assertNotNull(new2Txt)
            val txt = new2Txt.read()!!.bufferedReader().use { it.readText() }
            assertEquals(tmpContent, txt)
            newTxt.delete()
            new2Txt.delete()
        }
    }

    @Test
    fun moveTest() {
        clientWithUser { client ->
            val tmpContent = Random.nextUuid().toString()
            client.new("/new.txt".toPath).bufferedAsciiWriter().use {
                it.append(tmpContent)
            }
            val newTxt = client.get("/new.txt".toPath)!!
            newTxt.move("/new2.txt".toPath)
            println("Coping done!")
            val new2Txt = client.get("/new2.txt".toPath)
            assertNotNull(new2Txt)
            val txt = new2Txt.read()!!.bufferedReader().use { it.readText() }
            assertEquals(tmpContent, txt)
            assertNull(client.get("/new.txt".toPath))
            new2Txt.delete()
        }
    }

    @Test
    fun getRangeTest() {
        clientWithUser { client ->
            val tmpContent = Random.nextUuid().toString()
            client.new("/new.txt".toPath).bufferedAsciiWriter().use {
                it.append(tmpContent)
            }
            val newTxt = client.get("/new.txt".toPath)!!
            val contentWithOffset = newTxt.read(offset = 2u)!!.bufferedAsciiReader().use { it.readText() }
            val contentWithLimit =
                newTxt.read(offset = 2u, length = 5u)!!.bufferedAsciiReader().use { it.readText() }
            newTxt.delete()
            assertEquals(tmpContent.substring(2), contentWithOffset)
            assertEquals(tmpContent.substring(2, endIndex = 5 + 2), contentWithLimit)
        }
    }

    @Test
    fun deleteAndGetFileTest() {
        clientWithUser { client ->
            val tmp = Random.nextUuid().toString()
            client.new("/new.txt".toPath).bufferedWriter().use { it.append(tmp) }
            val newTxt = client.get("/new.txt".toPath)
            assertNotNull(newTxt)
            println("newTxt=${newTxt}")
            newTxt.delete()
            assertTrue(client.getDir("/".toPath)!!.toList().isEmpty())

            try {
                newTxt.delete()
                fail()
            } catch (e: FileSystem.FileNotFoundException) {
                //Do nothing
            }
            assertNull(client.get("/new.txt".toPath))
        }
    }

    @Test
    fun cyrillicNamingTest() {
        clientWithUser { client ->
            assertNull(client.get("/привет 2.txt".toPath))
            val txt = "Привет мир"
            client.new("/привет .txt".toPath).bufferedWriter(charset = Charsets.get("windows-1251"))
                .use { it.append(txt) }
            val helloTxt = client.get("/привет .txt".toPath)
            assertNotNull(helloTxt)
            assertEquals(txt.length.toLong(), helloTxt.length)
            assertTrue(helloTxt.isFile)
            helloTxt.delete()
        }
    }
}