package pw.binom.wasm

import pw.binom.io.Input
import pw.binom.io.file.File
import pw.binom.io.file.openRead
import pw.binom.io.use
import pw.binom.wasm.visitors.CodeSectionVisitor
import pw.binom.wasm.visitors.CustomSectionVisitor
import pw.binom.wasm.visitors.FunctionSectionVisitor
import pw.binom.wasm.visitors.ImportSectionVisitor
import pw.binom.wasm.visitors.TypeSectionVisitor
import kotlin.test.Test

class ReadFromFileTest {
  @Test
  fun aaa() {
//    val path = "/home/subochev/tmp/wasm-test/c/dot.o"
//    val path = "/home/subochev/tmp/wasm-test/build/compileSync/wasmWasi/main/developmentExecutable/kotlin/www-wasm-wasi.wasm"
//    val path = "/home/subochev/tmp/wasm-test/build/compileSync/wasmWasi/main/productionExecutable/kotlin/www-wasm-wasi.wasm"
    val path =
      "/home/subochev/tmp/wasm-test/build/compileSync/wasmJs/main/developmentExecutable/kotlin/www-wasm-js.wasm"
//    val path = "/home/subochev/tmp/wasm-test/build/compileSync/wasmJs/main/productionExecutable/kotlin/www-wasm-js.wasm"
//    val path = "/home/subochev/tmp/wasm-test/build/compileSync/wasmJs/main/productionExecutable/optimized/www-wasm-js.wasm"
    File(path)
      .openRead()
      .use { buf ->
        WasmReader.read(StreamReader(buf), MyWasmVisitor())
      }
  }
}

class MyWasmVisitor : WasmVisitor {
  override fun start() {
  }

  override fun end() {
  }

  override fun importSection(): ImportSectionVisitor = MyImportSectionVisitor()
  override fun functionSection(): FunctionSectionVisitor = MyFunctionSectionVisitor()
  override fun codeVisitor(): CodeSectionVisitor = MyCodeSectionVisitor()
  override fun typeSection(): TypeSectionVisitor = MyTypeSectionVisitor()
  override fun customSection(): CustomSectionVisitor = MyCustomSectionVisitor()
  override fun startSection(function: FunctionId) {
  }
}

class MyCustomSectionVisitor : CustomSectionVisitor {
  override fun start(name: String) {
    println("Custom section name: $name")
  }

  override fun data(input: Input) {
    input.skipAll()
  }

  override fun end() {
  }
}

class MyFunctionSectionVisitor : FunctionSectionVisitor {
  override fun start() {
  }

  override fun value(int: UInt) {
    println("function($int)")
  }

  override fun end() {
  }

}

class MyTypeSectionVisitor : TypeSectionVisitor {
  private val args = ArrayList<ValueType>()
  private val results = ArrayList<ValueType>()
  private var index = -1
  override fun start() {
    args.clear()
    results.clear()
    index++
  }

  override fun argument(type: ValueType) {
    args += type
  }

  override fun result(type: ValueType) {
    results += type
  }

  override fun end() {
    args.clear()
    results.clear()
  }

}

class MyImportSectionVisitor : ImportSectionVisitor {
  override fun start() {
  }

  override fun end() {
  }

  override fun function(module: String, field: String, index: UInt) {
    println("Import $module->$field function: #$index")
  }

  override fun memory(module: String, field: String, initial: UInt, maximum: UInt) {
    println("Import $module->$field memory with range $initial->$maximum")
  }

  override fun memory(module: String, field: String, initial: UInt) {
    println("Import $module->$field memory with range $initial->unlimited")
  }

  override fun table(module: String, field: String, type: ValueType.Ref, min: UInt, max: UInt) {
    println("Import $module->$field table $type with range $min->$max")
  }

  override fun table(module: String, field: String, type: ValueType.Ref, min: UInt) {
    println("Import $module->$field table $type with range $min->unlimited")
  }
}

class MyCodeSectionVisitor() : CodeSectionVisitor {
  private var c = 0
  override fun start(size: UInt) {
    c++
    println("---------------Start CODE (size=$size) $c---------------")
  }

  override fun end() {
  }

  override fun indexArgument(opcode: UByte, index: Int) {
  }

  override fun memOpAlignOffsetArg(opcode: UByte, readVarUInt32AsInt: Int, readVarUInt32: Long) {
  }

  override fun numOp(opcode: UByte) {
  }

  override fun controlFlow(opcode: UByte, type: ValueType?) {
  }

  override fun controlFlow(opcode: UByte, labelIndex: Int) {
  }

  override fun controlFlow(opcode: UByte) {
  }

  override fun const(opcode: UByte, fromBits: Float) {
  }

  override fun const(opcode: UByte, value: Int) {
    println("const $value")
  }

  override fun compare(opcode: UByte) {
  }

  override fun convert(opcode: UByte) {
  }

  override fun local(type: ValueType) {
  }

}
