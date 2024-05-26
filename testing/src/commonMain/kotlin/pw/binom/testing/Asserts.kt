package pw.binom.testing

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
import kotlin.test.*

fun <T : Iterable<*>> T.shouldBeNotEmpty(message: String? = null): T {
  if (!iterator().hasNext()) {
    fail(message ?: "Collection is empty")
  }
  return this
}

fun <T : Iterable<*>> T.shouldBeEmpty(message: String? = null): T {
  if (iterator().hasNext()) {
    fail(message ?: "Collection is not empty")
  }
  return this
}

@OptIn(ExperimentalContracts::class)
fun <T : Any> T?.shouldNotNull(message: String? = null): T {
  contract { returns() implies (this@shouldNotNull != null) }
  assertNotNull(actual = this, message = message)
  return this
}

@OptIn(ExperimentalContracts::class)
fun <T : Any> T?.shouldNull(message: String? = null): T? {
  contract { returns() implies (this@shouldNull == null) }
  assertNull(actual = this, message = message)
  return null
}

infix fun <T, R : T> T.shouldEquals(expected: R): T {
  assertEquals(expected = expected, actual = this)
  return this
}

@OptIn(ExperimentalContracts::class)
fun Boolean.shouldBeTrue(message: String? = null): Boolean {
  contract {
    returns() implies this@shouldBeTrue
  }
  assertTrue(actual = this, message = message)
  return this
}

@OptIn(ExperimentalContracts::class)
fun Boolean.shouldBeFalse(message: String? = null): Boolean {
  contract {
    returns() implies !this@shouldBeFalse
  }
  assertFalse(actual = this, message = message)
  return this
}

infix fun <T> Array<T>?.shouldContentEquals(expected: Array<T>?) {
  assertContentEquals(expected = expected, actual = this)
}

infix fun <T> Sequence<T>?.shouldContentEquals(expected: Sequence<T>?) {
  assertContentEquals(expected = expected, actual = this)
}

infix fun ByteArray?.shouldContentEquals(expected: ByteArray?) {
  assertContentEquals(expected = expected, actual = this)
}

infix fun ShortArray?.shouldContentEquals(expected: ShortArray?) {
  assertContentEquals(expected = expected, actual = this)
}

infix fun IntArray?.shouldContentEquals(expected: IntArray?) {
  assertContentEquals(expected = expected, actual = this)
}

infix fun LongArray?.shouldContentEquals(expected: LongArray?) {
  assertContentEquals(expected = expected, actual = this)
}

infix fun FloatArray?.shouldContentEquals(expected: FloatArray?) {
  assertContentEquals(expected = expected, actual = this)
}

infix fun DoubleArray?.shouldContentEquals(expected: DoubleArray?) {
  assertContentEquals(expected = expected, actual = this)
}

infix fun BooleanArray?.shouldContentEquals(expected: BooleanArray?) {
  assertContentEquals(expected = expected, actual = this)
}

infix fun CharArray?.shouldContentEquals(expected: CharArray?) {
  assertContentEquals(expected = expected, actual = this)
}

@OptIn(ExperimentalUnsignedTypes::class)
infix fun UByteArray?.shouldContentEquals(expected: UByteArray?) {
  assertContentEquals(expected = expected, actual = this)
}

@OptIn(ExperimentalUnsignedTypes::class)
infix fun UShortArray?.shouldContentEquals(expected: UShortArray?) {
  assertContentEquals(expected = expected, actual = this)
}

@OptIn(ExperimentalUnsignedTypes::class)
infix fun UIntArray?.shouldContentEquals(expected: UIntArray?) {
  assertContentEquals(expected = expected, actual = this)
}

@OptIn(ExperimentalUnsignedTypes::class)
infix fun ULongArray?.shouldContentEquals(expected: ULongArray?) {
  assertContentEquals(expected = expected, actual = this)
}
