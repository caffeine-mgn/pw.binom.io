package pw.binom.xml

import kotlinx.coroutines.test.runTest
import pw.binom.io.asAsync
import pw.binom.io.asReader
import kotlin.test.*

class AsyncXmlLexerTest {

  @Test
  fun test() = runTest {
    val lexer = AsyncXmlLexer(
      """<name="KDE"/><title>
&quot;KDE-DE&quot;
</title>
      """.trimMargin().asReader().asAsync(),
    )
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
  }
}

suspend fun AsyncXmlLexer.nextText(): String? {
  return if (next()) {
    text
  } else {
    null
  }
}
