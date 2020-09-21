package pw.binom.xml

import pw.binom.async
import pw.binom.io.asAsync
import pw.binom.io.asReader
import kotlin.test.*

class AsyncXmlLexerTest {

    @Test
    fun test() {
        val lexer = AsyncXmlLexer("""<name="KDE"/><title>
&quot;KDE-DE&quot;
</title>""".trimMargin().asReader().asAsync())
        var exception: Throwable? = null
        async {
            try {
                assertEquals("<", lexer.nextText())
                assertEquals("name", lexer.nextText())
                assertEquals("=", lexer.nextText())
                assertEquals("\"KDE\"", lexer.nextText())
                assertEquals("/", lexer.nextText())
                assertEquals(">", lexer.nextText())
                assertEquals("<", lexer.nextText())
                assertEquals("title", lexer.nextText())
                assertEquals(">", lexer.nextText())
                assertEquals("\n", lexer.nextText())
                assertEquals("&", lexer.nextText())
                assertEquals("quot", lexer.nextText())
                assertEquals(";", lexer.nextText())
                assertEquals("KDE-DE", lexer.nextText())
                assertEquals("&", lexer.nextText())
                assertEquals("quot", lexer.nextText())
                assertEquals(";", lexer.nextText())
                assertEquals("\n", lexer.nextText())
                assertEquals("<", lexer.nextText())
                assertEquals("/", lexer.nextText())
                assertEquals("title", lexer.nextText())
                assertEquals(">", lexer.nextText())
                assertFalse(lexer.isEof)
                assertNull(lexer.nextText())
                assertTrue(lexer.isEof)

                assertEquals(2, lexer.line)
                assertEquals(8, lexer.column)
                assertEquals(48, lexer.position)
            } catch (e: Throwable) {
                exception = e
            }
        }
        if (exception != null)
            throw exception!!
    }
}

suspend fun AsyncXmlLexer.nextText(): String {
    return if (next()) {
        text
    } else {
        ""
    }
}