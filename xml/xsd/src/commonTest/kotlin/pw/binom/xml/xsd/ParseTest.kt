package pw.binom.xml.xsd

import kotlinx.coroutines.test.runTest
import pw.binom.io.StringReader
import pw.binom.io.asAsync
import pw.binom.io.file.File
import pw.binom.io.file.readText
import kotlin.test.Ignore
import kotlin.test.Test

class ParseTest {
    @Ignore
    @Test
    fun test() = runTest {
        val txt =
            File("C:\\Users\\subochev\\Downloads\\Telegram Desktop\\okb-uch-master@7b993d95b3f\\xsd\\UCH758Types.xsd")
                .readText()

        XsdSchema.parse(StringReader(txt).asAsync())

//        val dom = StringReader(txt).asAsync().xmlTree()
//        dom.childs.forEach {
//            println("-->$it")
//        }
//        println("dom: $dom")
    }
}
