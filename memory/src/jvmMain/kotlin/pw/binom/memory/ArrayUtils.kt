@file:JvmName("ArrayUtilsJvmKt")
package pw.binom.memory

actual fun ByteArray.fill(value: Byte) {
  fill(element = value)
}
