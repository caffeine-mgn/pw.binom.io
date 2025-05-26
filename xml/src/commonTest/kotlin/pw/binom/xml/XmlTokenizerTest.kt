package pw.binom.xml

import pw.binom.io.Reader
import pw.binom.io.StringReader
import pw.binom.xml.SyncXmlTokenizer.EOFException
import pw.binom.xml.sax.SyncXmlVisitor2
import kotlin.test.Test

class XmlTokenizerTest {

  @Test
  fun test() {
    val tok = SyncXmlTokenizer(SyncBufferedReader(StringReader(data), 10))
    while (tok.next()) {
      println("${tok.text} ${tok.type}")
    }
  }

  @Test
  fun test3() {
    val visitor = XmlXElementDomVisitor()
    XmlParser.parse(
      tokenizer = SyncXmlTokenizer(StringReader(data)),
      visitor = visitor
    )
    println(visitor)
    println(data)
    visitor.elements.forEach {
      println(it)
    }
  }

  @Test
  fun test2() {
    XmlParser.parse(
      tokenizer = SyncXmlTokenizer(SyncBufferedReader(StringReader(data), 10)),
      visitor = object : SyncXmlVisitor2 {
        override fun startOpenTag(tagName: String) {
          println("startOpenTag($tagName)")
        }

        override fun endOpenTag(endTag: Boolean) {
          println("endOpenTag($endTag)")
        }

        override fun endTag(tagName: String) {
          println("endTag($tagName)")
        }

        override fun attribute(name: String, value: String) {
          println("attribute($name,$value)")
        }

        override fun comment(body: String) {
          println("comment($body)")
        }

        override fun cdata(body: String) {
          println("cdata($body)")
        }

        override fun text(body: String) {
          println("text($body)")
        }
      }
    )
  }

  class ReaderWithBuffer(val reader: Reader, bufferSize: Int) : Reader {
    private var eof = false
    private val buffer = CharBuffer(bufferSize) {
      position--
    }
    private var position = 0
    fun push(value: Char) {
      buffer.push(value)
    }

    override fun read(): Char? {
      if (eof) {
        return null
      }
      val resultChar = if (buffer.isEmpty) {
        try {
          reader.read()
        } catch (e: EOFException) {
          eof = true
          throw e
        }
      } else {
        buffer.get()
      }
      if (resultChar == null) {
        eof = true
        return null
      }
      position++
      return resultChar
    }

    override fun close() {
      reader.close()
    }
  }

  @Test
  fun fff() {
    val buffer = CharBuffer(4)
    val buf = ReaderWithBuffer(StringReader("ANTON"), 10)
    SyncXmlTokenizer.readString("ANTO1N", readChar = { buf.read() ?: throw EOFException() }, pushBack = { buf.push(it) })
    val sb = StringBuilder()
    while (true) {
      sb.append(buf.read() ?: break)
    }
    println(sb)
  }


  val data = """
    <?xml version="1.0" encoding="UTF-8"?>
    <div class="wbr t-title">
    <br/>
    <!--some-comment-->
    <![CDATA[<sender>John Smith</sender>]]>
      <a data-topic_id="6682891" class="med tLink tt-text ts-text hl-tags bold" href="viewtopic.php?t=6682891">[RM] [restored] [declipped] [16/44] (Progressive Metal) Tool - Дискография (9 releases) - 1991-2019, FLAC (tracks+cue)</a>
    </div>
  """.trimIndent()
}
