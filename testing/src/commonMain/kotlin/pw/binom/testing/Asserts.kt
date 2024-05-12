package pw.binom.testing

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
import kotlin.test.*

fun <T : Iterable<*>> T.shouldBeNotEmpty(): T {
  if (!iterator().hasNext()) {
    fail("Collection is empty")
  }
  return this
}

fun <T : Iterable<*>> T.shouldBeEmpty(): T {
  if (iterator().hasNext()) {
    fail("Collection is not empty")
  }
  return this
}

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

infix fun <T, R : T> T.shouldEquals(expected: R): T {
  assertEquals(expected, this)
  return this
}

@OptIn(ExperimentalContracts::class)
fun Boolean.shouldBeTrue(): Boolean {
  contract {
    returns() implies this@shouldBeTrue
  }
  assertTrue(this)
  return this
}

@OptIn(ExperimentalContracts::class)
fun Boolean.shouldBeFalse(): Boolean {
  contract {
    returns() implies !this@shouldBeFalse
  }
  assertFalse(this)
  return this
}

infix fun <T> Array<T>?.shouldContentEquals(expected: Array<T>?) {
  assertContentEquals(expected, this)
}

infix fun <T> Sequence<T>?.shouldContentEquals(expected: Sequence<T>?) {
  assertContentEquals(expected, this)
}

infix fun ByteArray?.shouldContentEquals(expected: ByteArray?) {
  assertContentEquals(expected, this)
}

infix fun ShortArray?.shouldContentEquals(expected: ShortArray?) {
  assertContentEquals(expected, this)
}

infix fun IntArray?.shouldContentEquals(expected: IntArray?) {
  assertContentEquals(expected, this)
}

infix fun LongArray?.shouldContentEquals(expected: LongArray?) {
  assertContentEquals(expected, this)
}

infix fun FloatArray?.shouldContentEquals(expected: FloatArray?) {
  assertContentEquals(expected, this)
}

infix fun DoubleArray?.shouldContentEquals(expected: DoubleArray?) {
  assertContentEquals(expected, this)
}

infix fun BooleanArray?.shouldContentEquals(expected: BooleanArray?) {
  assertContentEquals(expected, this)
}

infix fun CharArray?.shouldContentEquals(expected: CharArray?) {
  assertContentEquals(expected, this)
}

@OptIn(ExperimentalUnsignedTypes::class)
infix fun UByteArray?.shouldContentEquals(expected: UByteArray?) {
  assertContentEquals(expected, this)
}

@OptIn(ExperimentalUnsignedTypes::class)
infix fun UShortArray?.shouldContentEquals(expected: UShortArray?) {
  assertContentEquals(expected, this)
}

@OptIn(ExperimentalUnsignedTypes::class)
infix fun UIntArray?.shouldContentEquals(expected: UIntArray?) {
  assertContentEquals(expected, this)
}

@OptIn(ExperimentalUnsignedTypes::class)
infix fun ULongArray?.shouldContentEquals(expected: ULongArray?) {
  assertContentEquals(expected, this)
}
