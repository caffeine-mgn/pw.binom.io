package pw.binom.wasm.readers

import pw.binom.io.use
import pw.binom.wasm.StreamReader
import pw.binom.wasm.WasmInput
import pw.binom.wasm.readValueType
import pw.binom.wasm.readVec
import pw.binom.wasm.visitors.CodeSectionVisitor

const val ALL = -1
const val NONE = -2

val BAD_FUNCTION_BLOCK = NONE
val BAD_OP = NONE

var readOpCount = 0
var readFunctionCount = 0
var writeFunctionCount = 0
var writeOpCount = 0
var lastWriteOpSize = 0

/**
 * https://webassembly.github.io/exception-handling/core/binary/modules.html#binary-codesec
 * https://webassembly.github.io/exception-handling/core/binary/instructions.html#binary-instr
 * https://webassembly.github.io/gc/core/binary/instructions.html#binary-instr
 * https://github.com/WebAssembly/design/blob/main/BinaryEncoding.md#function-bodies
 * https://chromium.googlesource.com/v8/v8/+/refs/heads/main/src/wasm/wasm-opcodes.h
 * https://www.w3.org/TR/wasm-core-2/
 */
object CodeSectionReader {
  fun read(input: WasmInput, visitor: CodeSectionVisitor) {
    visitor.start()
    input.readVec {
      read(input = input, visitor = visitor.code())
    }
    visitor.end()
  }

  private fun read(input: WasmInput, visitor: CodeSectionVisitor.CodeVisitor) {
    input as StreamReader
    val cur = input.globalCursor
    val sizeInBytes = input.v32u()
    visitor.start()
    readFunctionCount++

    if (sizeInBytes == 0u) {
      visitor.end()
      return
    }
    input.withLimit(sizeInBytes).use { sectionInput ->
      sectionInput.readVec {
        val size = sectionInput.v32u()
        sectionInput.readValueType(visitor = visitor.local(size))
      }
      val before = input.globalCursor
      readOpCount = 0
      ExpressionReader.readExpressions(input = sectionInput, visitor = visitor.code())
      if (readFunctionCount == BAD_FUNCTION_BLOCK || BAD_FUNCTION_BLOCK == ALL) {
        println("READ FUNCTION $readFunctionCount size: $sizeInBytes. codeSize: ${input.globalCursor - before} on 0x${cur.toString(16)}")
      }
    }
    visitor.end()
  }
}
