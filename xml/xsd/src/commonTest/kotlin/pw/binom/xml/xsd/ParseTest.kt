package pw.binom.xml.xsd

import kotlinx.coroutines.test.runTest
import pw.binom.io.*
import pw.binom.io.file.File
import pw.binom.io.file.openRead
import kotlin.test.Ignore
import kotlin.test.Test

class ParseTest {
    @Ignore
    @Test
    fun test() = runTest {
        val txt =
            File("C:\\Users\\subochev\\Downloads\\Telegram Desktop\\okb-uch-master@7b993d95b3f\\xsd\\UCH758Types.xsd")
                .openRead()
                .bufferedReader()
                .use { it.readText() }

        XsdSchema.parse(StringReader(txt).asAsync())

//        val dom = StringReader(txt).asAsync().xmlTree()
//        dom.childs.forEach {
//            println("-->$it")
//        }
//        println("dom: $dom")
    }
}
