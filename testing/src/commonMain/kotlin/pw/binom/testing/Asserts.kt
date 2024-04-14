package pw.binom.testing

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@OptIn(ExperimentalContracts::class)
fun <T : Any> T?.shouldNotNull(): T {
  contract { returns() implies (this@shouldNotNull != null) }
  assertNotNull(this)
  return this
}

@OptIn(ExperimentalContracts::class)
fun <T : Any> T?.shouldNull(): T? {
  contract { returns() implies (this@shouldNull == null) }
  assertNull(this)
  return null
}

fun <T, R : T> T.shouldEquals(expected: R) {
  assertEquals(expected, this)
}

fun <T> Array<T>?.shouldContentEquals(expected: Array<T>?) {
  assertContentEquals(expected, this)
}

fun <T> Sequence<T>?.shouldContentEquals(expected: Sequence<T>?) {
  assertContentEquals(expected, this)
}

fun ByteArray?.shouldContentEquals(expected: ByteArray?) {
  assertContentEquals(expected, this)
}

fun ShortArray?.shouldContentEquals(expected: ShortArray?) {
  assertContentEquals(expected, this)
}

fun IntArray?.shouldContentEquals(expected: IntArray?) {
  assertContentEquals(expected, this)
}

fun LongArray?.shouldContentEquals(expected: LongArray?) {
  assertContentEquals(expected, this)
}

fun FloatArray?.shouldContentEquals(expected: FloatArray?) {
  assertContentEquals(expected, this)
}

fun DoubleArray?.shouldContentEquals(expected: DoubleArray?) {
  assertContentEquals(expected, this)
}

fun BooleanArray?.shouldContentEquals(expected: BooleanArray?) {
  assertContentEquals(expected, this)
}

fun CharArray?.shouldContentEquals(expected: CharArray?) {
  assertContentEquals(expected, this)
}

@OptIn(ExperimentalUnsignedTypes::class)
fun UByteArray?.shouldContentEquals(expected: UByteArray?) {
  assertContentEquals(expected, this)
}

@OptIn(ExperimentalUnsignedTypes::class)
fun UShortArray?.shouldContentEquals(expected: UShortArray?) {
  assertContentEquals(expected, this)
}

@OptIn(ExperimentalUnsignedTypes::class)
fun UIntArray?.shouldContentEquals(expected: UIntArray?) {
  assertContentEquals(expected, this)
}

@OptIn(ExperimentalUnsignedTypes::class)
fun ULongArray?.shouldContentEquals(expected: ULongArray?) {
  assertContentEquals(expected, this)
}
