package pw.binom.wasm

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

const val TYPE_NUM_I32: UByte = 0x7Fu
const val TYPE_NUM_I64: UByte = 0x7Eu
const val TYPE_NUM_F32: UByte = 0x7Du
const val TYPE_NUM_F64: UByte = 0x7Cu
const val TYPE_PAK_I8: UByte = 0x78u
const val TYPE_PAK_I16: UByte = 0x77u
const val TYPE_PAK_F16: UByte = 0x76u
const val TYPE_VEC_V128: UByte = 0x7Bu
const val TYPE_REF_FUNC_REF: UByte = 0x70u
const val TYPE_REF_EXTERN_REF: UByte = 0x6Fu
const val TYPE_REF_NULL: UByte = 0x63u
const val TYPE_REF: UByte = 0x64u
const val kStructRefCode: UByte = 0x6bu
const val kNoneCode: UByte = 0x71u
const val kAnyRefCode: UByte = 0x6eu

/**
 * reftype
 *
 * https://www.w3.org/TR/wasm-core-2/#binary-reftype
 * https://webassembly.github.io/gc/core/binary/types.html#reference-types
 */
fun InputReader.readRefType() =
  when (val value = readByte().toUByte()) {
    TYPE_REF_FUNC_REF -> ValueType.Ref.FUNC_REF
    TYPE_REF_EXTERN_REF -> ValueType.Ref.EXTERN_REF
    kStructRefCode -> ValueType.Ref.STRUCT
    kNoneCode -> ValueType.Ref.NONE
    else -> TODO("Unknown type 0x${value.toString(16)}")
  }

/**
 * https://www.w3.org/TR/wasm-core-2/#binary-resulttype
 */
inline fun InputReader.readResultType(func: (ValueType) -> Unit) {
  readVec {
    func(readValueType())
  }
}

fun InputReader.readStorageType() =
  when (val value = readByte().toUByte()) {
    TYPE_NUM_I32 -> ValueType.Num.I32
    TYPE_NUM_I64 -> ValueType.Num.I64
    TYPE_NUM_F32 -> ValueType.Num.F32
    TYPE_NUM_F64 -> ValueType.Num.F64
    TYPE_VEC_V128 -> ValueType.Vector.V128
    TYPE_REF_FUNC_REF -> ValueType.Ref.FUNC_REF
    TYPE_REF_EXTERN_REF -> ValueType.Ref.EXTERN_REF
    TYPE_REF_NULL -> {
      readHeapType()
      ValueType.Ref.NULL
    }

    TYPE_REF -> {
      readHeapType()
      ValueType.Ref.VALUE
    }

    TYPE_PAK_I8 -> StorageType.Packed.I8
    TYPE_PAK_I16 -> StorageType.Packed.I16
    TYPE_PAK_F16 -> StorageType.Packed.F16
    kStructRefCode -> ValueType.Ref.STRUCT
    else -> TODO("Unknown type 0x${value.toString(16)}")
  }

/**
 * https://webassembly.github.io/gc/core/binary/types.html#binary-heaptype
 */
fun InputReader.readHeapType() {
  readSignedLeb128()
//  readLebSigned()
}

/**
 * valtype
 *
 * https://www.w3.org/TR/wasm-core-2/#binary-valtype
 */
fun InputReader.readValueType(byte: UByte = readUByte()) =
  when (val value = byte) {
    TYPE_NUM_I32 -> ValueType.Num.I32
    TYPE_NUM_I64 -> ValueType.Num.I64
    TYPE_NUM_F32 -> ValueType.Num.F32
    TYPE_NUM_F64 -> ValueType.Num.F64
    TYPE_VEC_V128 -> ValueType.Vector.V128
    TYPE_REF_FUNC_REF -> ValueType.Ref.FUNC_REF
    TYPE_REF_EXTERN_REF -> ValueType.Ref.EXTERN_REF
    TYPE_REF_NULL -> {
      readHeapType()
      ValueType.Ref.NULL
    }

    kNoneCode -> ValueType.Ref.NONE
    kAnyRefCode -> ValueType.Ref.ANY
    TYPE_REF -> {
      readHeapType()
      ValueType.Ref.VALUE
    }

    else -> TODO("Unknown type 0x${value.toString(16)}")
  }

fun isValueType(byte:UByte)=
  when (byte) {
    TYPE_NUM_I32,
    TYPE_NUM_I64,
    TYPE_NUM_F32,
    TYPE_NUM_F64,
    TYPE_VEC_V128,
    TYPE_REF_FUNC_REF,
    TYPE_REF_EXTERN_REF,
    TYPE_REF_NULL,

    kNoneCode,
    kAnyRefCode -> true
    else -> false
  }
