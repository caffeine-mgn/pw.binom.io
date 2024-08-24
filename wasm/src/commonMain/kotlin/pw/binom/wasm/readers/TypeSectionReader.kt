package pw.binom.wasm.readers

import pw.binom.wasm.*
import pw.binom.wasm.visitors.TypeSectionVisitor
import pw.binom.wasm.visitors.ValueVisitor

/**
 * https://webassembly.github.io/exception-handling/core/binary/modules.html#type-section
 * https://www.w3.org/TR/wasm-core-2/#type-section%E2%91%A0
 * https://github.com/WebAssembly/gc/blob/main/proposals/gc/MVP.md
 * https://webassembly.github.io/gc/core/binary/modules.html#type-section
 */
object TypeSectionReader {
  //  private const val kWasmFunctionTypeCode: UByte = 0x60u
//  private const val kWasmRecursiveTypeGroupCode: UByte = 0x60u
  private const val kSharedFlagCode: UByte = 0x65u
  private const val kWasmFunctionTypeCode: UByte = 0x60u
  private const val kWasmStructTypeCode: UByte = 0x5fu
  private const val kWasmArrayTypeCode: UByte = 0x5eu
  private const val kWasmSubtypeCode: UByte = 0x50u
  private const val kWasmSubtypeFinalCode: UByte = 0x4fu
  private const val kWasmRecursiveTypeGroupCode: UByte = 0x4eu

  private fun consume_sig(input: WasmInput, visitor: TypeSectionVisitor) {
    input.readVec {
      // args
      input.readValueType(visitor = ValueVisitor.SKIP)
    }
    input.readVec {
      // results
      input.readValueType(visitor = ValueVisitor.SKIP)
    }
  }

  private fun consume_mutability(input: WasmInput): Boolean {
    val f = input.uByte()
    return when (f) {
      0u.toUByte() -> false
      1u.toUByte() -> true
//      else->true
      else -> TODO("f=0x${f.toString(16)}")
    }
  }

  private fun consume_struct(input: WasmInput, visitor: TypeSectionVisitor) {
    input.readVec {
      val storageType = input.readStorageType()
      println("storageType=$storageType")
      val mutable = input.v1u()
      println("mutable=$mutable")
    }
  }

  private fun consume_array(input: WasmInput, visitor: TypeSectionVisitor) {
    input.readStorageType()
    val mutable = input.v1u()
  }

  private fun consume_base_type_definition(kind: UByte?, input: WasmInput, visitor: TypeSectionVisitor) {
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
      kWasmFunctionTypeCode -> consume_sig(input = input, visitor = visitor)
      kWasmStructTypeCode -> {
        // is_wasm_gc=true
        consume_struct(input = input, visitor = visitor)
      }

      kWasmArrayTypeCode -> {
        // is_wasm_gc = true
        consume_array(input = input, visitor = visitor)
      }

      else -> TODO("Unknown kind 0x${kind.toString(16)}")
    }
  }

  private fun consume_subtype_definition(kind: UByte?, input: WasmInput, visitor: TypeSectionVisitor) {
    val kind = kind ?: input.uByte()
    if (kind == kWasmSubtypeCode || kind == kWasmSubtypeFinalCode) {
      kind == kWasmSubtypeFinalCode
      // is_wasm_gc = true
      val supertype_count = input.v32u()
      var supertype = UInt.MAX_VALUE
      if (supertype_count == 1u) {
        supertype = input.v32u()
        println("supertype=$supertype")
      } else {
        println("supertype not defined, supertype_count: $supertype_count")
      }
      // pass to visitor `consume_base_type_definition` and `is_final`
      consume_base_type_definition(kind = null, input = input, visitor = visitor)
    } else {
      consume_base_type_definition(kind = kind, input = input, visitor = visitor)
    }
  }

  private fun DecodeTypeSection(input: WasmInput, visitor: TypeSectionVisitor) {
    val groupKind = input.uByte()
    if (groupKind == kWasmRecursiveTypeGroupCode) {
      // is_wasm_gc = true
      input.readVec {
        consume_subtype_definition(input = input, visitor = visitor, kind = null)
      }
    } else {
      consume_subtype_definition(input = input, visitor = visitor, kind = groupKind)
    }
  }

  fun read(input: WasmInput, visitor: TypeSectionVisitor) {
    DecodeTypeSection(input = input, visitor = visitor)
//    check(byte == kWasmFunctionTypeCode) {
//      "Invalid marker 0x${byte.toString(16)}, position=0x${
//        (input.globalCursor - 1).toUInt().toString(16)
//      } (${input.globalCursor - 1})"
//    }
  }
}
