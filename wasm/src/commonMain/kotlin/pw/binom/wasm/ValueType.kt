package pw.binom.wasm

import pw.binom.wasm.visitors.AbsHeapType
import pw.binom.wasm.visitors.ValueVisitor

//sealed interface Types {
//  enum class Numers:Types {
//    I32,
//    I64,
//  }
//  object I32 : Types
//  object I64 : Types
//  class Ref(val type: TypeId) : Types
//}


sealed interface ValueType : StorageType {
  sealed interface Num : ValueType {
    data object I32 : Num
    data object I64 : Num
    data object F32 : Num
    data object F64 : Num
  }

  sealed interface Ref : ValueType {
    data object FUNC_REF : Ref
    data object EXTERN_REF : Ref
    data object NONE : Ref
    data object ANY : Ref
    data object STRUCT : Ref
    data object NULL : Ref
    data object VALUE : Ref
  }

  sealed interface Vector : ValueType {
    data object V128 : Vector
  }
}

sealed interface StorageType {
  interface Packed : StorageType {
    data object I8 : Packed
    data object I16 : Packed
    data object F16 : Packed
  }
}


/**
 * reftype
 *
 * https://www.w3.org/TR/wasm-core-2/#binary-reftype
 * https://webassembly.github.io/gc/core/binary/types.html#reference-types
 */
fun StreamReader.readRefType() =
  when (val value = readByte().toUByte()) {
    Types.TYPE_REF_ABS_HEAP_FUNC_REF -> ValueType.Ref.FUNC_REF
    Types.TYPE_REF_EXTERN_REF -> ValueType.Ref.EXTERN_REF
    Types.TYPE_REF_ABS_HEAP_STRUCT -> ValueType.Ref.STRUCT
    Types.TYPE_REF_ABS_HEAP_NONE -> ValueType.Ref.NONE
    else -> TODO("Unknown type 0x${value.toString(16)}")
  }

fun StreamReader.readStorageType() =
  when (val value = readUByte()) {
    Types.TYPE_NUM_I32 -> ValueType.Num.I32
    Types.TYPE_NUM_I64 -> ValueType.Num.I64
    Types.TYPE_NUM_F32 -> ValueType.Num.F32
    Types.TYPE_NUM_F64 -> ValueType.Num.F64
    Types.TYPE_VEC_V128 -> ValueType.Vector.V128
    Types.TYPE_REF_ABS_HEAP_FUNC_REF -> ValueType.Ref.FUNC_REF
    Types.TYPE_REF_EXTERN_REF -> ValueType.Ref.EXTERN_REF
    Types.TYPE_REF_NULL -> {
      readHeapType(visitor = ValueVisitor.HeapVisitor.EMPTY)
      ValueType.Ref.NULL
    }

    Types.TYPE_REF -> {
      readHeapType(visitor = ValueVisitor.HeapVisitor.EMPTY)
      ValueType.Ref.VALUE
    }

    Types.TYPE_PAK_I8 -> StorageType.Packed.I8
    Types.TYPE_PAK_I16 -> StorageType.Packed.I16
    Types.TYPE_PAK_F16 -> StorageType.Packed.F16
    Types.TYPE_REF_ABS_HEAP_STRUCT -> ValueType.Ref.STRUCT
    else -> TODO("Unknown type 0x${value.toString(16)}")
  }

//fun StreamReader.readAbsHeapType(byte: UByte = readUByte()): UByte {
//  check(Types.isAbsHeapType(byte))
//  return byte
//}

fun StreamReader.readAbsHeapType(byte: UByte = readUByte()) = when (byte) {
  Types.TYPE_REF_ABS_HEAP_NO_FUNC -> AbsHeapType.TYPE_REF_ABS_HEAP_NO_FUNC
  Types.TYPE_REF_ABS_HEAP_NO_EXTERN -> AbsHeapType.TYPE_REF_ABS_HEAP_NO_FUNC
  Types.TYPE_REF_ABS_HEAP_NONE -> AbsHeapType.TYPE_REF_ABS_HEAP_NO_FUNC
  Types.TYPE_REF_ABS_HEAP_FUNC_REF -> AbsHeapType.TYPE_REF_ABS_HEAP_NO_FUNC
  Types.TYPE_REF_ABS_HEAP_EXTERN -> AbsHeapType.TYPE_REF_ABS_HEAP_NO_FUNC
  Types.TYPE_REF_ABS_HEAP_ANY -> AbsHeapType.TYPE_REF_ABS_HEAP_NO_FUNC
  Types.TYPE_REF_ABS_HEAP_EQ -> AbsHeapType.TYPE_REF_ABS_HEAP_NO_FUNC
  Types.TYPE_REF_ABS_HEAP_I31 -> AbsHeapType.TYPE_REF_ABS_HEAP_NO_FUNC
  Types.TYPE_REF_ABS_HEAP_STRUCT -> AbsHeapType.TYPE_REF_ABS_HEAP_NO_FUNC
  Types.TYPE_REF_ABS_HEAP_ARRAY -> AbsHeapType.TYPE_REF_ABS_HEAP_NO_FUNC
  else -> TODO()
}

fun StreamReader.readHeapType(
  byte: UByte = readUByte(),
  visitor: ValueVisitor.HeapVisitor,
) {
  if (Types.isAbsHeapType(byte)) {
    visitor.type(readAbsHeapType(byte))
  } else {
    visitor.type(TypeId(v33s(byte.toByte()).toUInt()))
  }
}

fun StreamReader.readRefType(
  byte: UByte = readUByte(),
  visitor: ValueVisitor.RefVisitor,
) {
  val firstByte = byte
  when (firstByte) {
    0x64u.toUByte() -> readHeapType(byte, visitor.ref())
    0x63u.toUByte() -> readHeapType(byte, visitor.refNull())
    else -> visitor.refNull(readAbsHeapType(byte))
  }
}

/**
 * https://webassembly.github.io/gc/core/binary/types.html#binary-heaptype
 */
//fun StreamReader.readHeapType(
//  byte: UByte = readUByte(),
//  absHeadType: (UByte) -> Unit,
//  type: (TypeId) -> Unit,
//) {
//  val firstByte = byte
//  if (Types.isAbsHeapType(firstByte)) {
//    absHeadType(readAbsHeapType(firstByte))
//  } else {
//    type(TypeId(v33s(firstByte.toByte()).toUInt()))
//  }
//}

/**
 * valtype
 *
 * https://www.w3.org/TR/wasm-core-2/#binary-valtype
 */
fun StreamReader.readValueType(byte: UByte = readUByte(), visitor: ValueVisitor) =
  when (val value = byte) {
    Types.TYPE_NUM_I32 -> visitor.numType().i32()
    Types.TYPE_NUM_I64 -> visitor.numType().i64()
    Types.TYPE_NUM_F32 -> visitor.numType().f32()
    Types.TYPE_NUM_F64 -> visitor.numType().f64()
    Types.TYPE_VEC_V128 -> visitor.vecType().v128()
    Types.TYPE_REF_ABS_HEAP_FUNC_REF -> visitor.refType().ref().type(AbsHeapType.TYPE_REF_ABS_HEAP_FUNC_REF)
    Types.TYPE_REF_EXTERN_REF -> visitor.refType().ref().type(AbsHeapType.TYPE_REF_ABS_HEAP_EXTERN)
    Types.TYPE_REF_NULL -> readHeapType(visitor = visitor.refType().refNull())

    Types.TYPE_REF_ABS_HEAP_NONE -> visitor.refType().ref().type(AbsHeapType.TYPE_REF_ABS_HEAP_NONE)
    Types.TYPE_REF_ABS_HEAP_ANY -> visitor.refType().ref().type(AbsHeapType.TYPE_REF_ABS_HEAP_ANY)
    Types.TYPE_REF -> readHeapType(visitor = visitor.refType().ref())

    else -> TODO("Unknown type 0x${value.toString(16)}")
  }

fun isValueType(byte: UByte) =
  when (byte) {
    Types.TYPE_NUM_I32,
    Types.TYPE_NUM_I64,
    Types.TYPE_NUM_F32,
    Types.TYPE_NUM_F64,
    Types.TYPE_VEC_V128,
    Types.TYPE_REF_ABS_HEAP_FUNC_REF,
    Types.TYPE_REF_EXTERN_REF,
    Types.TYPE_REF_NULL,
    Types.TYPE_REF_ABS_HEAP_NONE,
    Types.TYPE_REF_ABS_HEAP_ANY,
      -> true

    else -> false
  }
