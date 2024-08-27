package pw.binom.wasm

import pw.binom.fromBytes
import pw.binom.io.ByteArrayInput
import pw.binom.io.ByteArrayOutput
import kotlin.test.Test


class AAAAA {
  @OptIn(ExperimentalStdlibApi::class)
  @Test
  fun ttt() {
    val e = InMemoryWasmOutput()
    e.v32u(14u)
    val ee = e.locked {
      val bytes = it.toByteArray()
      println("hex=${bytes.toHexString()}")
      StreamReader(ByteArrayInput(bytes)).v32u()
    }
    println("ee=$ee")
  }
}
