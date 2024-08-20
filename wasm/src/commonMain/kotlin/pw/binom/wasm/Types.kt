package pw.binom.wasm

/**
 * [GC Types](https://webassembly.github.io/gc/core/binary/types.html)
 */
object Types {
  const val TYPE_NUM_I32: UByte = 0x7Fu
  const val TYPE_NUM_I64: UByte = 0x7Eu
  const val TYPE_NUM_F32: UByte = 0x7Du
  const val TYPE_NUM_F64: UByte = 0x7Cu
  const val TYPE_PAK_I8: UByte = 0x78u
  const val TYPE_PAK_I16: UByte = 0x77u
  const val TYPE_PAK_F16: UByte = 0x76u
  const val TYPE_VEC_V128: UByte = 0x7Bu
  const val TYPE_REF_EXTERN_REF: UByte = 0x6Fu
  const val TYPE_REF_NULL: UByte = 0x63u
  const val TYPE_REF: UByte = 0x64u
  const val TYPE_REF_ABS_HEAP_ARRAY: UByte = 0x6Au
  const val TYPE_REF_ABS_HEAP_STRUCT: UByte = 0x6bu
  const val TYPE_REF_ABS_HEAP_I31: UByte = 0x6cu
  const val TYPE_REF_ABS_HEAP_EQ: UByte = 0x6du
  const val TYPE_REF_ABS_HEAP_ANY: UByte = 0x6eu
  const val TYPE_REF_ABS_HEAP_EXTERN: UByte = 0x6fu
  const val TYPE_REF_ABS_HEAP_FUNC_REF: UByte = 0x70u
  const val TYPE_REF_ABS_HEAP_NONE: UByte = 0x71u
  const val TYPE_REF_ABS_HEAP_NO_EXTERN: UByte = 0x72u
  const val TYPE_REF_ABS_HEAP_NO_FUNC: UByte = 0x73u

  fun isAbsHeapType(value: UByte) = when (value) {
    TYPE_REF_ABS_HEAP_NO_FUNC,
    TYPE_REF_ABS_HEAP_NO_EXTERN,
    TYPE_REF_ABS_HEAP_NONE,
    TYPE_REF_ABS_HEAP_FUNC_REF,
    TYPE_REF_ABS_HEAP_EXTERN,
    TYPE_REF_ABS_HEAP_ANY,
    TYPE_REF_ABS_HEAP_EQ,
    TYPE_REF_ABS_HEAP_I31,
    TYPE_REF_ABS_HEAP_STRUCT,
    TYPE_REF_ABS_HEAP_ARRAY,
      -> true

    else -> false
  }

  fun isVecType(value: UByte) = when (value) {
    TYPE_VEC_V128 -> true
    else -> false
  }

  fun isNumberType(value: UByte) = when (value) {
    TYPE_NUM_I32,
    TYPE_NUM_I64,
    TYPE_NUM_F32,
    TYPE_NUM_F64,
      -> true

    else -> false
  }
}
