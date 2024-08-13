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



/**
 * reftype
 *
 * https://www.w3.org/TR/wasm-core-2/#binary-reftype
 * https://webassembly.github.io/gc/core/binary/types.html#reference-types
 */
fun StreamReader.readRefType() =
  when (val value = readByte().toUByte()) {
    Types.TYPE_REF_FUNC_REF -> ValueType.Ref.FUNC_REF
    Types.TYPE_REF_EXTERN_REF -> ValueType.Ref.EXTERN_REF
    Types.TYPE_REF_HEAP_STRUCT -> ValueType.Ref.STRUCT
    Types.TYPE_REF_HEAP_NONE -> ValueType.Ref.NONE
    else -> TODO("Unknown type 0x${value.toString(16)}")
  }

/**
 * https://www.w3.org/TR/wasm-core-2/#binary-resulttype
 */
inline fun StreamReader.readResultType(func: (ValueType) -> Unit) {
  readVec {
    func(readValueType())
  }
}

fun StreamReader.readStorageType() =
  when (val value = readByte().toUByte()) {
    Types.TYPE_NUM_I32 -> ValueType.Num.I32
    Types.TYPE_NUM_I64 -> ValueType.Num.I64
    Types.TYPE_NUM_F32 -> ValueType.Num.F32
    Types.TYPE_NUM_F64 -> ValueType.Num.F64
    Types.TYPE_VEC_V128 -> ValueType.Vector.V128
    Types.TYPE_REF_FUNC_REF -> ValueType.Ref.FUNC_REF
    Types.TYPE_REF_EXTERN_REF -> ValueType.Ref.EXTERN_REF
    Types.TYPE_REF_NULL -> {
      readHeapType()
      ValueType.Ref.NULL
    }

    Types.TYPE_REF -> {
      readHeapType()
      ValueType.Ref.VALUE
    }

    Types.TYPE_PAK_I8 -> StorageType.Packed.I8
    Types.TYPE_PAK_I16 -> StorageType.Packed.I16
    Types.TYPE_PAK_F16 -> StorageType.Packed.F16
    Types.TYPE_REF_HEAP_STRUCT -> ValueType.Ref.STRUCT
    else -> TODO("Unknown type 0x${value.toString(16)}")
  }

/**
 * https://webassembly.github.io/gc/core/binary/types.html#binary-heaptype
 */
fun StreamReader.readHeapType() {
  v32s()
//  readLebSigned()
}

/**
 * valtype
 *
 * https://www.w3.org/TR/wasm-core-2/#binary-valtype
 */
fun StreamReader.readValueType(byte: UByte = readUByte()) =
  when (val value = byte) {
    Types.TYPE_NUM_I32 -> ValueType.Num.I32
    Types.TYPE_NUM_I64 -> ValueType.Num.I64
    Types.TYPE_NUM_F32 -> ValueType.Num.F32
    Types.TYPE_NUM_F64 -> ValueType.Num.F64
    Types.TYPE_VEC_V128 -> ValueType.Vector.V128
    Types.TYPE_REF_FUNC_REF -> ValueType.Ref.FUNC_REF
    Types.TYPE_REF_EXTERN_REF -> ValueType.Ref.EXTERN_REF
    Types.TYPE_REF_NULL -> {
      readHeapType()
      ValueType.Ref.NULL
    }

    Types.TYPE_REF_HEAP_NONE -> ValueType.Ref.NONE
    Types.TYPE_REF_ANY -> ValueType.Ref.ANY
    Types.TYPE_REF -> {
      readHeapType()
      ValueType.Ref.VALUE
    }

    else -> TODO("Unknown type 0x${value.toString(16)}")
  }

fun isValueType(byte:UByte)=
  when (byte) {
    Types.TYPE_NUM_I32,
    Types.TYPE_NUM_I64,
    Types.TYPE_NUM_F32,
    Types.TYPE_NUM_F64,
    Types.TYPE_VEC_V128,
    Types.TYPE_REF_FUNC_REF,
    Types.TYPE_REF_EXTERN_REF,
    Types.TYPE_REF_NULL,
    Types.TYPE_REF_HEAP_NONE,
    Types.TYPE_REF_ANY -> true
    else -> false
  }
