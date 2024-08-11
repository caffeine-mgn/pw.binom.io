package pw.binom.wasm

//fun InputReader.toValueType() = toValueType(readVarInt7())
//fun InputReader.toValueType(type: Byte):ValueType{
////  println("(-0x01).toByte()=0x${(-0x01).toByte().toUByte().toString(16)}")
////  println("(-0x02).toByte()=0x${(-0x02).toByte().toUByte().toString(16)}")
////  println("(-0x03).toByte()=0x${(-0x03).toByte().toUByte().toString(16)}")
////  println("(-0x04).toByte()=0x${(-0x04).toByte().toUByte().toString(16)}")
//  return when (type) {
//    (-0x01).toByte() -> ValueType.I32
//    (-0x02).toByte() -> ValueType.I64
//    (-0x03).toByte() -> ValueType.F32
//    (-0x04).toByte() -> ValueType.F64
////    (-0x1d).toByte() -> ValueType.RefNull
////      else -> Primitive.UNKNOWN
//    else -> error("Unknown value type: $type (0x${type.toString(16)}, ${type.toString(16)})")
//  }
//}

internal const val INT_MASK = 0xffffffffL

fun Long.unsignedToSignedInt(): Int {
  if (this and INT_MASK != this) throw NumberFormatException()
  return this.toInt()
}

fun Byte.toUnsignedShort() = (this.toInt() and 0xff).toShort()
