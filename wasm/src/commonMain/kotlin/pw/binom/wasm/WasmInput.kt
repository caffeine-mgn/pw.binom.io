package pw.binom.wasm

import pw.binom.io.Input
import pw.binom.wasm.visitors.AbsHeapType
import pw.binom.wasm.visitors.ExpressionsVisitor
import pw.binom.wasm.visitors.ValueVisitor
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

interface WasmInput : Input {
  fun withLimit(limit: UInt): WasmInput
  fun v32u(): UInt
  fun v32s(): Int
  fun v64u(): ULong
  fun v64s(): Long
  fun v33u(): ULong
  fun i32s(): Int
  fun i64s(): Long
  fun v33s(firstByte: Byte = sByte()): Long
  fun v1u(): Boolean
  fun string(): String
  fun skipOther()
  fun sByte(): Byte
  fun uByte(): UByte
}

inline fun WasmInput.readVec(func: () -> Unit) {
  var count = v32u()
  while (count > 0u) {
    count--
    func()
  }
}

@OptIn(ExperimentalContracts::class)
inline fun WasmInput.readLimit(min: (UInt) -> Unit, range: (UInt, UInt) -> Unit) {
  contract {
    callsInPlace(min, InvocationKind.AT_MOST_ONCE)
    callsInPlace(range, InvocationKind.AT_MOST_ONCE)
  }
  val limitExist = v1u()
  val min = v32u()
  if (!limitExist) {
    min(min)
    return
  }
  val max = v32u()
  range(min, max)
}

fun WasmInput.readRefType(
  byte: UByte = uByte(),
  visitor: ValueVisitor.RefVisitor,
) {
  val firstByte = byte
  when (firstByte) {
    0x64u.toUByte() -> readHeapType(byte, visitor.ref())
    0x63u.toUByte() -> readHeapType(byte, visitor.refNull())
    else -> visitor.refNull(readAbsHeapType(byte))
  }
}

fun WasmInput.readHeapType(
  byte: UByte = uByte(),
  visitor: ValueVisitor.HeapVisitor,
) {
  if (Types.isAbsHeapType(byte)) {
    visitor.type(readAbsHeapType(byte))
  } else {
    visitor.type(TypeId(v33s(byte.toByte()).toUInt()))
  }
}

fun WasmInput.readAbsHeapType(byte: UByte = uByte()) = when (byte) {
  Types.TYPE_REF_ABS_HEAP_NO_FUNC -> AbsHeapType.TYPE_REF_ABS_HEAP_NO_FUNC
  Types.TYPE_REF_ABS_HEAP_NO_EXTERN -> AbsHeapType.TYPE_REF_ABS_HEAP_NO_EXTERN
  Types.TYPE_REF_ABS_HEAP_NONE -> AbsHeapType.TYPE_REF_ABS_HEAP_NONE
  Types.TYPE_REF_ABS_HEAP_FUNC_REF -> AbsHeapType.TYPE_REF_ABS_HEAP_FUNC_REF
  Types.TYPE_REF_ABS_HEAP_EXTERN -> AbsHeapType.TYPE_REF_ABS_HEAP_EXTERN
  Types.TYPE_REF_ABS_HEAP_ANY -> AbsHeapType.TYPE_REF_ABS_HEAP_ANY
  Types.TYPE_REF_ABS_HEAP_EQ -> AbsHeapType.TYPE_REF_ABS_HEAP_EQ
  Types.TYPE_REF_ABS_HEAP_I31 -> AbsHeapType.TYPE_REF_ABS_HEAP_I31
  Types.TYPE_REF_ABS_HEAP_STRUCT -> AbsHeapType.TYPE_REF_ABS_HEAP_STRUCT
  Types.TYPE_REF_ABS_HEAP_ARRAY -> AbsHeapType.TYPE_REF_ABS_HEAP_ARRAY
  else -> TODO()
}

fun WasmInput.readNumType(byte: UByte = uByte(), visitor: ValueVisitor.NumberVisitor) {
  when (byte) {
    Types.TYPE_NUM_I32 -> visitor.i32()
    Types.TYPE_NUM_I64 -> visitor.i64()
    Types.TYPE_NUM_F32 -> visitor.f32()
    Types.TYPE_NUM_F64 -> visitor.f64()
    else -> TODO()
  }
}

fun WasmInput.readVecType(byte: UByte = uByte(), visitor: ValueVisitor.VectorVisitor) {
  when (byte) {
    Types.TYPE_VEC_V128 -> visitor.v128()
    else -> TODO()
  }
}

/**
 * valtype
 *
 * https://www.w3.org/TR/wasm-core-2/#binary-valtype
 */
fun WasmInput.readValueType(byte: UByte = uByte(), visitor: ValueVisitor) =
  when (val value = byte) {
    Types.TYPE_NUM_I32,
    Types.TYPE_NUM_I64,
    Types.TYPE_NUM_F32,
    Types.TYPE_NUM_F64,
      -> readNumType(byte = byte, visitor = visitor.numType())

    Types.TYPE_VEC_V128 -> readVecType(byte = byte, visitor = visitor.vecType())

    Types.TYPE_REF_ABS_HEAP_FUNC_REF -> visitor.refType(AbsHeapType.TYPE_REF_ABS_HEAP_FUNC_REF)
    Types.TYPE_REF_EXTERN_REF -> visitor.refType(AbsHeapType.TYPE_REF_ABS_HEAP_EXTERN)
    Types.TYPE_REF_ABS_HEAP_NONE -> visitor.refType(AbsHeapType.TYPE_REF_ABS_HEAP_NONE)
    Types.TYPE_REF_ABS_HEAP_ANY -> visitor.refType(AbsHeapType.TYPE_REF_ABS_HEAP_ANY)

    Types.TYPE_REF_NULL -> readHeapType(visitor = visitor.refType().refNull())
    Types.TYPE_REF -> readHeapType(visitor = visitor.refType().ref())

    else -> TODO("Unknown type 0x${value.toString(16)}")
  }

fun WasmInput.readStorageType() =
  when (val value = uByte()) {
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

fun WasmInput.readBlockType(visitor: ExpressionsVisitor.BlockStartVisitor) {
  val firstByte1 = v33u()
  val firstByte = firstByte1.toUByte()
  if (firstByte == 0x40u.toUByte()) {
    visitor.withoutType()
  } else {
    if (isValueType(firstByte)) {
      readValueType(byte = firstByte, visitor = visitor.valueType())
    } else {
      TODO()
      val index = v32s()
      println("block type index: $index")
//        readUnsignedLeb128(4)
    }
  }
}
