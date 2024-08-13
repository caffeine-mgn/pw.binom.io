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
  const val TYPE_REF_FUNC_REF: UByte = 0x70u
  const val TYPE_REF_EXTERN_REF: UByte = 0x6Fu
  const val TYPE_REF_NULL: UByte = 0x63u
  const val TYPE_REF: UByte = 0x64u
  const val TYPE_REF_HEAP_STRUCT: UByte = 0x6bu
  const val TYPE_REF_HEAP_NONE: UByte = 0x71u
  const val TYPE_REF_ANY: UByte = 0x6eu
}
