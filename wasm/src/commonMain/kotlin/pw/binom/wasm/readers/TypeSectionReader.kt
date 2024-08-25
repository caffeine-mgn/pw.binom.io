package pw.binom.wasm.readers

import pw.binom.wasm.*
import pw.binom.wasm.visitors.TypeSectionVisitor

/**
 * https://webassembly.github.io/exception-handling/core/binary/modules.html#type-section
 * https://www.w3.org/TR/wasm-core-2/#type-section%E2%91%A0
 * https://github.com/WebAssembly/gc/blob/main/proposals/gc/MVP.md
 * https://webassembly.github.io/gc/core/binary/modules.html#type-section
 */
object TypeSectionReader {
  //  private const val kWasmFunctionTypeCode: UByte = 0x60u
//  private const val kWasmRecursiveTypeGroupCode: UByte = 0x60u
  const val kSharedFlagCode: UByte = 0x65u
  const val kWasmFunctionTypeCode: UByte = 0x60u
  const val kWasmStructTypeCode: UByte = 0x5fu
  const val kWasmArrayTypeCode: UByte = 0x5eu
  const val kWasmSubtypeCode: UByte = 0x50u
  const val kWasmSubtypeFinalCode: UByte = 0x4fu
  const val kWasmRecursiveTypeGroupCode: UByte = 0x4eu

  private fun readFuncType(input: WasmInput, shared: Boolean, visitor: TypeSectionVisitor.FuncTypeVisitor) {
    visitor.start(shared = shared)
    input.readVec {
      // args
      input.readValueType(visitor = visitor.arg())
    }
    input.readVec {
      // results
      input.readValueType(visitor = visitor.result())
    }
    visitor.end()
  }

  private fun readStructType(input: WasmInput, shared: Boolean, visitor: TypeSectionVisitor.StructTypeVisitor) {
    visitor.start(shared = shared)
    input.readVec {
      input.readStorageType(visitor = visitor.fieldStart())
      val mutable = input.v1u()
      visitor.fieldEnd(mutable)
    }
    visitor.end()
  }

  private fun readArrayType(input: WasmInput, shared: Boolean, visitor: TypeSectionVisitor.ArrayVisitor) {
    visitor.start(shared = shared)
    input.readStorageType(visitor = visitor.type())
    visitor.mutable(value = input.v1u())
    visitor.end()
  }

  private fun readCompType(
    kind: UByte?,
    input: WasmInput,
    visitor: TypeSectionVisitor.CompositeTypeVisitor,
  ) {
    var kind = kind ?: input.uByte()
    var shared = false
    if (kind == kSharedFlagCode) {
      println("is shared")
      shared = true
      kind = input.uByte()
    } else {
      println("is not shared")
    }
    when (kind) {
      kWasmArrayTypeCode -> {
        // is_wasm_gc = true
        readArrayType(input = input, shared = shared, visitor = visitor.array())
      }

      kWasmStructTypeCode -> {
        // is_wasm_gc=true
        readStructType(input = input, shared = shared, visitor = visitor.struct())
      }

      kWasmFunctionTypeCode -> readFuncType(input = input, shared = shared, visitor = visitor.function())

      else -> TODO("Unknown kind 0x${kind.toString(16)}")
    }
  }

  private fun readSubType(byte: UByte?, input: WasmInput, visitor: TypeSectionVisitor.SubTypeVisitor) {
    val kind = byte ?: input.uByte()
    if (kind == kWasmSubtypeCode || kind == kWasmSubtypeFinalCode) {
      val isFinal = kind == kWasmSubtypeFinalCode
      // is_wasm_gc = true
      val v = if (isFinal) {
        visitor.withParentFinal()
      } else {
        visitor.withParent()
      }
      input.readVec {
        v.parent(TypeId(input.v32u())) // super-types
      }
      readCompType(kind = null, input = input, visitor = v.type())
    } else {
      readCompType(kind = kind, input = input, visitor = visitor.single())
    }
  }

  private fun DecodeTypeSection(input: WasmInput, visitor: TypeSectionVisitor.RecTypeVisitor) {
    val groupKind = input.uByte()
    if (groupKind == kWasmRecursiveTypeGroupCode) {
      // is_wasm_gc = true
      val v = visitor.recursive()
      v.start()
      input.readVec {
        readSubType(input = input, visitor = v.type(), byte = null)
      }
      v.end()
    } else {
      readSubType(input = input, visitor = visitor.single(), byte = groupKind)
    }
  }

  fun read(input: WasmInput, visitor: TypeSectionVisitor) {
    visitor.start()
    val r = visitor.recType()
    input.readVec {
      DecodeTypeSection(input = input, visitor = r)
    }
    visitor.end()
  }
}
