package pw.binom.io.http

import kotlinx.coroutines.test.runTest
import pw.binom.asyncInput
import pw.binom.asyncOutput
import pw.binom.copyTo
import pw.binom.io.*
import pw.binom.readByte
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class AsyncChunkedInputTest {

  @Test
  fun test2() = runTest {
    val output = ByteArrayOutput()
    val chunked = AsyncChunkedOutput(output.asyncOutput())

    val sb = chunked.utf8Appendable()
    sb.append("Wiki")
    chunked.flush()
    sb.append("pedia")
    sb.asyncClose()

    val out = output.data
    out.flip()

    val input = AsyncChunkedInput(out.asyncInput())
    assertEquals("Wikipedia", input.utf8Reader().readText())
  }

  fun ByteArrayOutput.appendChunk(data: ByteArray) {
    write(data.size.toString(16).encodeToByteArray())
    writeByte(CR)
    writeByte(LF)
    write(data)
    writeByte(CR)
    writeByte(LF)
  }

  fun ByteArrayOutput.appendChunk(text: String) {
    appendChunk(text.encodeToByteArray())
  }

  fun ByteArrayOutput.finishChunk() {
    appendChunk(ByteArray(0))
  }

  private suspend fun AsyncInput.readChar() = ByteBuffer(1).use { buffer ->
    readByte(buffer)
  }.toInt().toChar()

  @Test
  fun shortDataTest() = runTest {
    val output = ByteBuffer(TestData.wikipediaChunkedData.size)
    TestData.wikipediaChunkedData.forEach {
      output.put(it.code.toByte())
    }
    output.clear()
    val input = AsyncChunkedInput(output.asAsyncChannel())

    TestData.wikipediaData.forEach {
      assertEquals(it, input.readChar())
    }

    ByteBuffer(1).use {
      assertEquals(DataTransferSize.CLOSED, input.read(it))
    }

    assertFalse(output.hasRemaining)
  }

  @Test
  fun bigDataTest() = runTest {
    val output = ByteArrayOutput()
    val part1 = Random.nextBytes(30)
    val part2 = Random.nextBytes(30)
    output.appendChunk(part1)
    output.appendChunk(part2)
    output.finishChunk()

    val input = AsyncChunkedInput(ByteArrayInput(output.toByteArray()).asyncInput())
    val o = ByteArrayOutput()
    input.copyTo(o.asyncOutput(), bufferSize = 10)
    assertContentEquals(part1 + part2, o.toByteArray())
  }
}
