package pw.binom.wasm.runner

import pw.binom.toByteArray
import kotlin.test.Test

class LongTest {
  @Test
  fun aaa(){

    val e = "А".encodeToByteArray().map { it.toUByte() }.joinToString("-")
    println(e)
    val b = 'А'.code.toByteArray().map { it.toUByte() }.joinToString("-")
    println(b)
    println('А'.code)
  }
  @Test
  fun test() {
    val max = 0b1111111111111111111111111111111111111111111111111111111111111111uL
    println("--->$max")
    println("------>${UInt.MAX_VALUE.toString(2)}")
  }

  fun Long.Companion.fromComponent(a: Int, b: Int): Long = a.toLong() shl 32 or b.toLong()
  val Long.left
    get() = ushr(32).toInt()
  val Long.right
    get() = (this and UInt.MAX_VALUE.toLong()).toInt()
}
