package pw.binom.wasm

import pw.binom.io.ByteArrayInput
import pw.binom.io.ByteBuffer
import pw.binom.io.use
import pw.binom.wrap
import kotlin.test.Test


fun ByteArray.from(pos: Int) = copyOfRange(fromIndex = pos, toIndex = size)

@OptIn(ExperimentalStdlibApi::class)
class AAAAA {

  @Test
  fun testRead87() {
    val bbb = ByteBuffer.wrap(byteArrayOf(-41, 0))
    println("READ: ${bbb.toByteArray().toHexString()}")
    val eee = StreamReader(bbb).use {
      println("before=${bbb.remaining}")
      val e = it.v32u()
      println("after=${bbb.remaining}")
      e
    }

    println("eee=$eee")


    val buffer = ByteBuffer(10)
    StreamWriter(buffer).v32u(87u)
    buffer.flip()
    println("WRITE: ${buffer.toByteArray().toHexString()}")
  }

  @Test
  fun ttt() {
    val e = InMemoryWasmOutput()
    e.v32u(11u)
    println("wrote size: ${e.size}")
    val ee = e.locked {
      val bytes = it.toByteArray()
      println("hex=${bytes.toHexString(HexFormat.UpperCase)}")
      StreamReader(ByteArrayInput(bytes)).use {
        it.v32u()
      }
    }

    println("ee=$ee")
  }
}
